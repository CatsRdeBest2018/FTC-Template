package org.firstinspires.ftc.teamcode.robot.helpers;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Immutable, unit-independent calibration. Construct once, then call {@link #get(double)}. */
public final class CalibrationTable {
    public enum Interpolation {
        LINEAR, MONOTONE_CUBIC, NEAREST, STEP,
        LINEAR_REGRESSION, QUADRATIC_REGRESSION, AUTO_FIT
    }

    public static final class Point {
        public final double input;
        public final double output;

        private Point(double input, double output) {
            this.input = input;
            this.output = output;
        }
    }

    public static Point point(double input, double output) {
        return new Point(input, output);
    }

    // Prefer simpler models when validation errors are within 5% of the best.
    private static final Interpolation[] CANDIDATES = {
            Interpolation.LINEAR_REGRESSION, Interpolation.LINEAR,
            Interpolation.NEAREST, Interpolation.STEP,
            Interpolation.QUADRATIC_REGRESSION, Interpolation.MONOTONE_CUBIC
    };

    private final double[] inputs;
    private final double[] outputs;
    private final double minimumOutput;
    private final double maximumOutput;
    private final Interpolation selectedMode;
    private final Model model;
    private final Map<Interpolation, Double> validationErrors;

    public CalibrationTable(Point... points) {
        this(Interpolation.LINEAR, points);
    }

    public CalibrationTable(Interpolation mode, Point... points) {
        this(mode, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, points);
    }

    /** Output limits apply to predictions, endpoint values, and AUTO_FIT validation. */
    public CalibrationTable(Interpolation mode, double minimumOutput,
                            double maximumOutput, Point... points) {
        if (mode == null || points == null || points.length < 2) {
            throw new IllegalArgumentException("Provide a mode and at least two calibration points");
        }
        if (Double.isNaN(minimumOutput) || Double.isNaN(maximumOutput)
                || minimumOutput > maximumOutput
                || minimumOutput == Double.POSITIVE_INFINITY
                || maximumOutput == Double.NEGATIVE_INFINITY) {
            throw new IllegalArgumentException("Provide ordered output limits");
        }
        this.minimumOutput = minimumOutput;
        this.maximumOutput = maximumOutput;
        inputs = new double[points.length];
        outputs = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            Point p = points[i];
            if (p == null || !Double.isFinite(p.input) || !Double.isFinite(p.output)
                    || (i > 0 && p.input <= inputs[i - 1])) {
                throw new IllegalArgumentException("Points must be finite with strictly increasing inputs");
            }
            inputs[i] = p.input;
            outputs[i] = p.output;
        }
        requireFinite(inputs[inputs.length - 1] - inputs[0]);
        EnumMap<Interpolation, Double> scores = new EnumMap<>(Interpolation.class);
        Interpolation chosen = mode;
        if (mode == Interpolation.AUTO_FIT) {
            chosen = Interpolation.LINEAR;
            if (points.length >= 4) {
                double best = Double.POSITIVE_INFINITY;
                for (Interpolation candidate : CANDIDATES) {
                    double score = validationError(candidate);
                    scores.put(candidate, score);
                    best = Math.min(best, score);
                }
                if (!Double.isFinite(best)) {
                    throw new IllegalArgumentException("Calibration data is numerically ill-conditioned");
                }
                double scale = 0;
                for (double output : outputs) scale = Math.max(scale, Math.abs(output));
                double allowance = best * 0.05 + 32 * Math.ulp(scale);
                for (Interpolation candidate : CANDIDATES) {
                    if (scores.get(candidate) - best <= allowance) {
                        chosen = candidate;
                        break;
                    }
                }
            }
        }
        selectedMode = chosen;
        model = new Model(chosen, inputs, outputs);
        validationErrors = Collections.unmodifiableMap(scores);
    }

    /**
     * Outside the measured range, returns the nearest measured endpoint output.
     * NEAREST midpoint ties choose the lower input; STEP changes at the exact next input.
     * Regression modes may differ from measured outputs inside the range.
     */
    public double get(double input) {
        if (!Double.isFinite(input)) throw new IllegalArgumentException("Input must be finite");
        if (input <= inputs[0]) return clip(outputs[0]);
        if (input >= inputs[inputs.length - 1]) return clip(outputs[outputs.length - 1]);
        return clip(model.get(input));
    }

    public Interpolation getSelectedMode() {
        return selectedMode;
    }

    /** Held-out RMSE in output units, or NaN for a manual mode / fewer than four points. */
    public double getValidationRmse() {
        Double score = validationErrors.get(selectedMode);
        return score == null ? Double.NaN : score;
    }

    /** Candidate RMSEs; an unusable numerical fit has positive infinity as its score. */
    public Map<Interpolation, Double> getValidationErrors() {
        return validationErrors;
    }

    private double clip(double value) {
        requireFinite(value);
        return Math.max(minimumOutput, Math.min(maximumOutput, value));
    }

    private double validationError(Interpolation candidate) {
        // Only omit interior points: omitting endpoints would test extrapolation/clamping.
        double norm = 0;
        try {
            for (int omitted = 1; omitted < inputs.length - 1; omitted++) {
                double[] x = new double[inputs.length - 1];
                double[] y = new double[outputs.length - 1];
                for (int source = 0, target = 0; source < inputs.length; source++) {
                    if (source == omitted) continue;
                    x[target] = inputs[source];
                    y[target++] = outputs[source];
                }
                double prediction = clip(new Model(candidate, x, y).get(inputs[omitted]));
                norm = Math.hypot(norm, prediction - outputs[omitted]);
            }
            // Ensure the final fit is usable too.
            new Model(candidate, inputs, outputs);
            return norm / Math.sqrt(inputs.length - 2);
        } catch (IllegalArgumentException error) {
            return Double.POSITIVE_INFINITY;
        }
    }

    private static void requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Calibration calculation exceeded finite numeric range");
        }
    }

    private static final class Model {
        private final Interpolation mode;
        private final double[] x;
        private final double[] y;
        private final double[] slopes;
        private final double[] coefficients;

        Model(Interpolation mode, double[] x, double[] y) {
            this.mode = mode;
            this.x = x;
            this.y = y;
            slopes = mode == Interpolation.MONOTONE_CUBIC ? cubicSlopes(x, y) : null;
            coefficients = mode == Interpolation.LINEAR_REGRESSION ? fit(1)
                    : mode == Interpolation.QUADRATIC_REGRESSION ? fit(2) : null;
        }

        double get(double input) {
            if (coefficients != null) {
                double z = normalized(input);
                double result = 0;
                for (int i = coefficients.length - 1; i >= 0; i--) {
                    result = result * z + coefficients[i];
                }
                return result;
            }
            int index = Arrays.binarySearch(x, input);
            if (index >= 0) return y[index];
            int upper = -index - 1;
            int lower = upper - 1;
            double h = x[upper] - x[lower];
            double t = (input - x[lower]) / h;
            switch (mode) {
                case STEP: return y[lower];
                case NEAREST: return t <= 0.5 ? y[lower] : y[upper];
                case MONOTONE_CUBIC:
                    double t2 = t * t;
                    double t3 = t2 * t;
                    double value = (2 * t3 - 3 * t2 + 1) * y[lower]
                            + (t3 - 2 * t2 + t) * h * slopes[lower]
                            + (-2 * t3 + 3 * t2) * y[upper]
                            + (t3 - t2) * h * slopes[upper];
                    // Protect the shape-preserving interval bound from rounding error.
                    requireFinite(value);
                    return Math.max(Math.min(y[lower], y[upper]),
                            Math.min(Math.max(y[lower], y[upper]), value));
                default: return (1 - t) * y[lower] + t * y[upper];
            }
        }

        private double normalized(double input) {
            return 2 * ((input - x[0]) / (x[x.length - 1] - x[0])) - 1;
        }

        // Least squares with normalized inputs and reorthogonalized QR, avoiding normal equations.
        private double[] fit(int degree) {
            int size = degree + 1;
            if (x.length < size) {
                throw new IllegalArgumentException("Quadratic regression needs at least three points");
            }
            double[][] q = new double[size][x.length];
            double[][] r = new double[size][size];
            double[] result = new double[size];
            for (int column = 0; column < size; column++) {
                for (int row = 0; row < x.length; row++) {
                    q[column][row] = Math.pow(normalized(x[row]), column);
                }
                for (int pass = 0; pass < 2; pass++) {
                    for (int previous = 0; previous < column; previous++) {
                        double projection = 0;
                        for (int row = 0; row < x.length; row++) {
                            projection += q[previous][row] * q[column][row];
                        }
                        r[previous][column] += projection;
                        for (int row = 0; row < x.length; row++) {
                            q[column][row] -= projection * q[previous][row];
                        }
                    }
                }
                double norm = 0;
                for (double value : q[column]) norm = Math.hypot(norm, value);
                if (norm < 1e-12) throw new IllegalArgumentException("Degenerate regression inputs");
                r[column][column] = norm;
                for (int row = 0; row < x.length; row++) {
                    q[column][row] /= norm;
                    result[column] += q[column][row] * y[row];
                }
            }
            for (int row = size - 1; row >= 0; row--) {
                for (int column = row + 1; column < size; column++) {
                    result[row] -= r[row][column] * result[column];
                }
                result[row] /= r[row][row];
                requireFinite(result[row]);
            }
            return result;
        }

        // Shape-preserving cubic Hermite slopes, including nonuniform input spacing.
        private static double[] cubicSlopes(double[] x, double[] y) {
            int n = x.length;
            double[] h = new double[n - 1];
            double[] d = new double[n - 1];
            double[] m = new double[n];
            for (int i = 0; i < n - 1; i++) {
                h[i] = x[i + 1] - x[i];
                d[i] = (y[i + 1] - y[i]) / h[i];
                requireFinite(d[i]);
            }
            m[0] = d[0];
            m[n - 1] = d[n - 2];
            for (int i = 1; i < n - 1; i++) {
                if (sameSign(d[i - 1], d[i])) {
                    double ratio = h[i - 1] / (h[i - 1] + h[i]);
                    double weight = (2 - ratio) / 3;
                    m[i] = 1 / (weight / d[i - 1] + (1 - weight) / d[i]);
                }
            }
            if (n > 2) {
                m[0] = endpointSlope(h[0], h[1], d[0], d[1]);
                m[n - 1] = endpointSlope(h[n - 2], h[n - 3], d[n - 2], d[n - 3]);
            }
            for (double slope : m) requireFinite(slope);
            return m;
        }

        private static double endpointSlope(double h0, double h1, double d0, double d1) {
            double slope = d0 + (h0 / (h0 + h1)) * (d0 - d1);
            if (!sameSign(slope, d0)) return 0;
            if (!sameSign(d0, d1) && Math.abs(slope) > 3 * Math.abs(d0)) return 3 * d0;
            return slope;
        }

        private static boolean sameSign(double a, double b) {
            return (a > 0 && b > 0) || (a < 0 && b < 0);
        }
    }
}
