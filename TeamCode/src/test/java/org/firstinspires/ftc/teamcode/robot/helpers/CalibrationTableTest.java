package org.firstinspires.ftc.teamcode.robot.helpers;

import static org.firstinspires.ftc.teamcode.robot.helpers.CalibrationTable.Interpolation.*;
import static org.firstinspires.ftc.teamcode.robot.helpers.CalibrationTable.point;
import static org.junit.Assert.*;

import org.junit.Test;

public class CalibrationTableTest {
    private static final double EPS = 1e-9;

    @Test public void linearUsesActualSpacingAndClampsEndpoints() {
        CalibrationTable table = new CalibrationTable(point(6, .20), point(12, .45), point(18, .80));
        assertEquals(.325, table.get(9), EPS);
        assertEquals(.45, table.get(12), 0);
        assertEquals(.20, table.get(-100), 0);
        assertEquals(.80, table.get(100), 0);
        CalibrationTable uneven = new CalibrationTable(point(0, 0), point(2, 4), point(10, 12));
        assertEquals(8, uneven.get(6), EPS);
    }

    @Test public void nearestAndStepHaveDefinedBoundaryBehavior() {
        CalibrationTable nearest = new CalibrationTable(NEAREST, point(0, 10), point(2, 20), point(4, 30));
        CalibrationTable step = new CalibrationTable(STEP, point(0, 10), point(2, 20), point(4, 30));
        assertEquals(10, nearest.get(1), 0);
        assertEquals(20, nearest.get(1.001), 0);
        assertEquals(10, step.get(1.999), 0);
        assertEquals(20, step.get(2), 0);
        assertEquals(30, step.get(4), 0);
    }

    @Test public void cubicMatchesKnownHermiteValuesAndIsSmoothAtKnots() {
        CalibrationTable table = new CalibrationTable(MONOTONE_CUBIC, point(0, 0), point(1, 1), point(2, 4));
        assertEquals(.3125, table.get(.5), EPS);
        assertEquals(2.1875, table.get(1.5), EPS);
        double delta = 1e-5;
        double leftSlope = (table.get(1) - table.get(1 - delta)) / delta;
        double rightSlope = (table.get(1 + delta) - table.get(1)) / delta;
        assertEquals(leftSlope, rightSlope, 1e-4);
    }

    @Test public void cubicPreservesIncreasingDecreasingFlatAndTurningIntervals() {
        double[] x = {0, .1, 2, 3, 8, 9};
        double[] y = {0, 2, 2, -3, -1, -1};
        CalibrationTable.Point[] points = new CalibrationTable.Point[x.length];
        for (int i = 0; i < x.length; i++) points[i] = point(x[i], y[i]);
        CalibrationTable table = new CalibrationTable(MONOTONE_CUBIC, points);
        for (int i = 0; i < x.length - 1; i++) {
            assertEquals(y[i], table.get(x[i]), 0);
            double previous = y[i];
            for (int j = 1; j <= 100; j++) {
                double value = table.get(x[i] + (x[i + 1] - x[i]) * j / 100);
                assertTrue(value >= Math.min(y[i], y[i + 1]) - EPS);
                assertTrue(value <= Math.max(y[i], y[i + 1]) + EPS);
                if (y[i + 1] >= y[i]) assertTrue(value >= previous - EPS);
                else assertTrue(value <= previous + EPS);
                previous = value;
            }
        }
    }

    @Test public void twoPointCubicIsLinear() {
        CalibrationTable table = new CalibrationTable(MONOTONE_CUBIC, point(2, 8), point(6, -4));
        assertEquals(5, table.get(3), EPS);
    }

    @Test public void regressionsFitKnownFunctionsWithLargeInputOffset() {
        double base = 1e9;
        CalibrationTable linear = new CalibrationTable(LINEAR_REGRESSION,
                point(base, 2), point(base + 1, 5), point(base + 2, 8), point(base + 3, 11));
        assertEquals(6.5, linear.get(base + 1.5), EPS);
        CalibrationTable quadratic = new CalibrationTable(QUADRATIC_REGRESSION,
                point(base, 2), point(base + 1, 6), point(base + 2, 12), point(base + 3, 20));
        assertEquals(8.75, quadratic.get(base + 1.5), EPS);
    }

    @Test public void regressionMaySmoothMeasurementsButClampsToMeasuredEndpoints() {
        CalibrationTable table = new CalibrationTable(LINEAR_REGRESSION,
                point(0, 0), point(1, 2), point(2, 2));
        assertEquals(4.0 / 3, table.get(1), EPS);
        assertEquals(0, table.get(-1), 0);
        assertEquals(0, table.get(0), 0);
        assertEquals(2, table.get(2), 0);
        assertEquals(2, table.get(3), 0);
    }

    @Test public void outputLimitsApplyToEveryModeAndEndpoints() {
        for (CalibrationTable.Interpolation mode : CalibrationTable.Interpolation.values()) {
            CalibrationTable table = new CalibrationTable(mode, 0, 1,
                    point(0, -2), point(1, -1), point(2, 2), point(3, 3));
            assertEquals(0, table.get(-1), 0);
            assertEquals(1, table.get(4), 0);
            for (int i = 0; i <= 30; i++) {
                double value = table.get(i / 10.0);
                assertTrue(value >= 0 && value <= 1);
            }
        }
    }

    @Test public void autoFitChoosesQuadraticUsingHeldOutErrors() {
        CalibrationTable table = new CalibrationTable(AUTO_FIT,
                point(0, 0), point(1, 1), point(2, 4), point(3, 9));
        assertEquals(QUADRATIC_REGRESSION, table.getSelectedMode());
        assertEquals(2.25, table.get(1.5), EPS);
        assertEquals(0, table.getValidationRmse(), EPS);
        // Omitting either interior point makes linear interpolation err by exactly 1.
        assertEquals(1, table.getValidationErrors().get(LINEAR), EPS);
        assertEquals(6, table.getValidationErrors().size());
        assertThrows(UnsupportedOperationException.class,
                () -> table.getValidationErrors().put(LINEAR, 99.0));
    }

    @Test public void autoFitPrefersSimpleModelsWhenScoresTie() {
        CalibrationTable line = new CalibrationTable(AUTO_FIT,
                point(0, 2), point(1, 5), point(2, 8), point(3, 11), point(4, 14));
        assertEquals(LINEAR_REGRESSION, line.getSelectedMode());
        assertEquals(0, line.getValidationRmse(), EPS);
        CalibrationTable constant = new CalibrationTable(AUTO_FIT,
                point(0, 5), point(1, 5), point(2, 5), point(3, 5));
        assertEquals(LINEAR_REGRESSION, constant.getSelectedMode());
        assertEquals(5, constant.get(1.5), EPS);
    }

    @Test public void autoFitFallsBackWithoutInventingAScoreForSmallTables() {
        CalibrationTable table = new CalibrationTable(AUTO_FIT, point(0, 0), point(1, 2), point(2, 3));
        assertEquals(LINEAR, table.getSelectedMode());
        assertEquals(1, table.get(.5), EPS);
        assertTrue(Double.isNaN(table.getValidationRmse()));
        assertTrue(table.getValidationErrors().isEmpty());
        assertTrue(Double.isNaN(new CalibrationTable(point(0, 0), point(1, 1)).getValidationRmse()));
    }

    @Test public void constructionCopiesCallerArray() {
        CalibrationTable.Point[] points = {point(0, 0), point(1, 1)};
        CalibrationTable table = new CalibrationTable(points);
        points[1] = point(1, 100);
        assertEquals(.5, table.get(.5), EPS);
    }

    @Test public void invalidDataAndQueriesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(point(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable((CalibrationTable.Point[]) null));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(point(1, 0), point(0, 1)));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(point(0, 0), point(0, 1)));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(point(0, 0), null));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(point(0, 0), point(1, Double.NaN)));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(point(0, 0), point(Double.POSITIVE_INFINITY, 1)));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(QUADRATIC_REGRESSION, point(0, 0), point(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> new CalibrationTable(LINEAR, 1, 0, point(0, 0), point(1, 1)));
        CalibrationTable table = new CalibrationTable(point(0, 0), point(1, 1));
        assertThrows(IllegalArgumentException.class, () -> table.get(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> table.get(Double.NEGATIVE_INFINITY));
    }
}
