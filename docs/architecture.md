# Project Architecture

- `robot/Robot.java` assembles hardware and owns all command factories in `Robot.Commands`.
- `robot/config/hardware/` stores device names and the expected configuration.
- `robot/subsystems/` stores reusable mechanism classes.
- `robot/helpers/` contains reusable control utilities such as PID.
- `opmodes/baseOpmodes/` owns shared robot, scheduler, telemetry, and stop behavior.
- `opmodes/routines/autos/` contains reusable autonomous routines.
- `opmodes/routines/tele/` contains shared TeleOp driving and button bindings.
- `opmodes/topAuto/blue/` and `opmodes/topAuto/red/` select an alliance for each routine.
- `opmodes/topTele/blue/` and `opmodes/topTele/red/` select an alliance and fallback pose for TeleOp.
- `opmodes/debug/` contains standalone hardware diagnostics.
- `pedro/` contains drivetrain/localizer settings and AutoTune procedures.
- `util/RobotTelemetry.java` sends readings to Panels and Driver Station.
- `pedro/FieldLocalizer.java` publishes measured poses to `util/LiveFieldView.java`.

## Creating an OpMode

The structure follows V3BioBuzz: `BaseOpMode` → `BaseAuto` → a shared routine →
small red/blue entry classes. For TeleOp, `BaseOpMode` → `BaseTele` → `DriverTele` → red/blue entries.
Only the final red/blue classes have `@Autonomous` or `@TeleOp` annotations.

The bases construct one robot named `r`, which always creates a Pedro follower.
The four drive motors and Pinpoint configured in `pedro/Constants.java` are required.
Override `createRobot()` if your robot needs additional subsystem initialization.

To add an autonomous routine:

1. Copy `routines/autos/ExampleAuto.java` and rename it for your routine.
2. Set `getStartingPose()` and return your Ivy command sequence in `buildRoutine()`.
3. Copy the blue and red entry classes, extend your routine, and give each a unique
   Driver Station name.

`ExampleAuto` is intentionally stationary until you add commands. Its starting pose
is a placeholder. Build paths inside `buildRoutine()`, after `Alliance.set()` has
run, rather than in static fields. `PoseLib.pose(x, y, headingDegrees)` takes RED-side
coordinates and rotates them 180 degrees about the center of a 144-inch field for
BLUE, matching the V3BioBuzz convention. Adapt this convention for your field.

Auto constructs its routine during INIT and schedules it only at START. The latest
auto pose is saved in `RobotVars` after updates and at STOP. The next same-alliance
TeleOp consumes it once; otherwise `getStartingPose()` provides the fallback. It is
session-only state and is cleared by a new Auto initialization or an app restart.
An Auto stopped before START does not publish a pose.

Add shared TeleOp button bindings in `routines/tele/DriverTele.handleControls()`.
Both alliance entries extend this routine. Change `DriverTele.drive()` for assisted
driving or other drive modes. Default driving is robot-relative and writes manual
inputs every loop. `BaseTele` handles pose initialization and handoff; controls live
in the routine.

Use Ivy directly: `command.schedule()` starts a command and `command.cancel()`
cancels it. Schedule from `start()`, `loop()`, or `handleControls()`, not INIT.
Keep a button-held command instance in a field so button release cancels that same
instance. There is no custom scheduling helper or command-tracking collection.

When overriding `init()`, `start()`, `loop()`, or `stop()`, call the superclass method.
The normal loop processes inputs, executes scheduled commands, updates the robot
once, and flushes telemetry. Do not call `r.follower.update()` a second time. STOP
resets the scheduler and calls `r.stop()`. Auto also cancels its routine explicitly.
Scheduler reset does not run command end callbacks; stop every added mechanism in
`Robot.stop()`, or explicitly cancel its command if it needs additional cleanup.

## Full robot and diagnostics

`Robot` always owns a Pedro follower:

```java
Robot robot = new Robot(hardwareMap);
```

`update()` updates the follower and enabled subsystems; `stop()` stops the follower
and zeros the four drive motors. The follower applies the drivetrain directions
in `pedro/Constants.java`. Direct `motorDriveXYVectors()` remains available for
diagnostics; avoid mixing it with follower updates, which write their own powers.

All robot command factories belong in the inner `Robot.Commands` class. It currently
provides `driveToPos()`, `followPath()`, and `holdPosition()`. Add future intake,
shooter, or combined mechanism commands there; each can access the enclosing robot's
subsystems directly. Subsystem classes keep hardware methods and control logic.
OpModes bind buttons and compose commands into autonomous sequences.

Commands that use a subsystem should declare `.requiring(subsystem)` and stop its
outputs in `.setEnd(...)` where appropriate. Keep a button-held command instance in
an OpMode field so the release handler cancels the same command that was scheduled.
Use `command.schedule()` and `command.cancel()` directly.

`Robot.update()` updates the follower and enabled subsystems. Robot Debugger writes motor power directly and calls only
`updateSubsystems()`, preventing the idle follower from overwriting debug outputs.
`Robot.stop()` stops drivetrain output immediately and stops the camera.

When extending the base classes, reuse `r` instead of constructing a second robot
for the same hardware. Construct mechanisms in `Robot`, update them in
`Robot.updateSubsystems()`, and stop them in `Robot.stop()`. Avoid updating them a
second time in OpMode callbacks.

## Telemetry and tuning

Debug configuration fields use Panels' `@Configurable`. `RobotTelemetry.begin()`
clears previous-session readings; call `update(telemetry)` once per loop to send readings
to both Panels and Driver Station. The bases do this automatically.

The Panels **Field** view updates automatically whenever a localizer created through
`Constants.createLocalizer()` updates, including every follower created through
`Constants.createFollower()`. No telemetry flush or base OpMode is required. It shows
robot position and heading using the Pedro Pathing coordinate preset and respects
Panels' field refresh interval.

For AutoTune, open Panels' Field view alongside the AutoTune page, then run Tests →
Localization Test. Keep that test running while watching the Field view; do not start
a second OpMode. Localization, odometry, pose, hold, line, curve, and interpolation
tests all use the shared localizer factory, as does Foresight tuning. Raw Driving Test
has no localizer, so it cannot provide a measured field position. The separate
sensor-specific calibration procedures use their own localizers.

AutoTune's tests still initialize their pose at `(0, 0, 0)`; the drawing uses that
coordinate origin and does not infer the robot's physical starting location. New code
should use the shared factories, or wrap a custom localizer in `FieldLocalizer` before
passing it to a follower. Updates draw the measured pose without updating hardware a
second time. The last pose stays visible when updates stop.

Full Panels includes the Limelight proxy. Pedro AutoTune retains its transitive Sloth
runtime dependency; no FTC Dashboard dependency is needed.

`PIDController` keeps the three-gain API and adds output-saturation anti-windup and
optional `kS`/`kV` velocity feedforward. Leave feedforward gains zero for ordinary
position control. Use `update(measurement)` for timed robot loops, or
`update(measurement, seconds)` for fixed-step loops and simulation; use one timing
style consistently within a controller run.

`CalibrationTable` provides linear, monotone cubic, nearest, and step interpolation,
linear/quadratic regression, and automatic selection using held-out prediction error.
Construct immutable tables in the subsystem that owns the measurements. See
[calibration tables](calibration-tables.md) for examples, output limits, and selection behavior.

Robot-specific mechanisms, field routes, alliance logic, motor directions, and
odometry calibration from BioBuzz are not included. Configure and tune the template
for the actual robot before driving its example paths.

## Validation

Run `./gradlew :TeamCode:assembleDebug :TeamCode:testDebugUnitTest`.
The JVM tests cover command scheduling and STOP cleanup, alliance pose transforms,
pose handoff, calibration interpolation, and automatic model selection. Physical
driving, localization, and Panels connectivity require a robot.
