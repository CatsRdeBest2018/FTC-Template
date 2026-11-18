# Driver Controls

`BlueDriverTele` and `RedDriverTele` both inherit `routines/tele/DriverTele`, which
extends `BaseTele` for pose initialization and handoff.

| Controller | Input | Action |
|---|---|---|
| Gamepad 1 | Left stick forward/back | Drive forward/backward |
| Gamepad 1 | Left stick left/right | Strafe left/right |
| Gamepad 1 | Right stick left/right | Turn left/right |

Driving is robot-relative. Verify motor directions with Wheel Debug on your robot.
Add mechanism controls in `DriverTele.handleControls()` and document them here.

TeleOp uses Pedro's manual drive inputs and requires the configured follower and
Pinpoint. Motor directions in `pedro/Constants.java` must match the drivetrain.

`Blue Example Auto` and `Red Example Auto` share `ExampleAuto`; the example only
prints a message at START and does not drive until you add routine commands.
