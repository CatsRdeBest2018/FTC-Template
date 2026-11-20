# Calibration tables

`robot/helpers/CalibrationTable.java` maps a measured input to a calibrated output.
It has no hardware, season, telemetry, or external library dependencies. Define
measured points in the subsystem that uses them, construct the table once during
initialization, then call `get(input)` in your normal update loop.

```java
import org.firstinspires.ftc.teamcode.robot.helpers.CalibrationTable;
import static org.firstinspires.ftc.teamcode.robot.helpers.CalibrationTable.point;
import static org.firstinspires.ftc.teamcode.robot.helpers.CalibrationTable.Interpolation.*;

// Illustrative values only; replace with measurements from your mechanism.
private final CalibrationTable heightToServo = new CalibrationTable(
        MONOTONE_CUBIC, 0.0, 1.0,
        point(6, 0.20),
        point(9, 0.31),
        point(12, 0.45),
        point(15, 0.61),
        point(18, 0.80)
);

// In the subsystem's positioning method:
// servo.setPosition(heightToServo.get(heightInches));
```

The two optional numbers before the points are minimum and maximum output limits.
Omit them for an unrestricted output range. Omitting the mode defaults to `LINEAR`.
Inputs and outputs can use any consistent units. Keep units clear in variable names.
The utility does not command hardware or limit movement speed over time.

## Modes

| Mode | Behavior |
| --- | --- |
| `LINEAR` | Straight lines between neighboring measurements. |
| `MONOTONE_CUBIC` | Smooth, shape-preserving cubic Hermite interpolation. Preserves each interval's increasing/decreasing/flat shape without overshooting its endpoint outputs. Two points reduce to a straight line. |
| `NEAREST` | Closest measured output; midpoint ties choose the lower input. |
| `STEP` | Previous measured output, changing at the exact next input threshold. |
| `LINEAR_REGRESSION` | Least-squares straight line across all measurements. Can smooth noisy data. |
| `QUADRATIC_REGRESSION` | Least-squares quadratic across all measurements; requires at least three points. |
| `AUTO_FIT` | Chooses among the six modes above using held-out prediction error. |

Interpolation modes reproduce the supplied points, subject to output limits.
Regression modes need not pass through interior measurements and can overshoot;
configure output limits appropriate for your mechanism.

All modes return the nearest **measured endpoint output** at and outside the input
boundaries, then apply output limits. They do not extrapolate. Because regression
does not necessarily pass through the endpoints, it can have a jump at a boundary;
use interpolation when continuity with measured endpoint values is required.

Points must have finite values and strictly increasing inputs, with at least two
points. Duplicate inputs, unsorted points, nulls, and nonfinite queries are rejected
with `IllegalArgumentException`. Numerically unrepresentable calculations are also
rejected. The table copies the supplied data and is immutable. Rebuild it explicitly
when applying new calibration measurements, rather than rebuilding every loop.

## Automatic selection

Use `AUTO_FIT` instead of the explicit mode in the example. Selection happens during
construction; `get()` only evaluates the selected model.

For each candidate, the table leaves out one interior point at a time, fits the
remaining points, and predicts the omitted output. It compares root mean squared
error (RMSE), in the same units as the outputs. Configured output limits apply to
predictions during validation too; errors are measured against the original data.
Endpoints always remain in the training data, so this evaluates interpolation
inside the measured range rather than extrapolation or boundary clamping.

Candidates within 5% of the lowest RMSE (plus floating-point rounding allowance)
are resolved in this preference order: linear regression, linear interpolation,
nearest, step, quadratic regression, monotone cubic. Numerically unusable candidates
receive an infinite error and are excluded. The selected model is refit using all
points. Fewer than four points fall back to linear interpolation without a score.

```java
telemetry.addData("Calibration model", heightToServo.getSelectedMode());
telemetry.addData("Validation RMSE", heightToServo.getValidationRmse());
telemetry.addData("Candidate errors", heightToServo.getValidationErrors());
```

Manual modes and small-table fallback return `NaN` for validation RMSE and an empty
candidate map. The map is read-only. Training R-squared is intentionally not used:
interpolators already pass through every training point, making that score unable
to distinguish their predictions between measurements.

Automatic selection is a heuristic, especially with few or unevenly spaced points.
It does not prove physical accuracy or score behavior outside the measured range.
Check additional measurements between existing points before relying on a chosen
curve, and retain mechanism-specific hardware limits in the subsystem.
