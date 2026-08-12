# Project Architecture

- `robot/Robot.java` assembles the complete robot and updates its subsystems.
- `robot/hardware/` stores shared hardware names and hardware-level configuration.
- `robot/subsystems/` stores one reusable class per robot capability or mechanism.
- `opmodes/teleop/` stores driver-controlled programs.
- `opmodes/autonomous/` stores autonomous routines.
- `opmodes/debug/` stores diagnostic programs.
- `pedroPathing/` stores Pedro Pathing configuration and tuning tools.

OpModes decide what the robot should do. Subsystems own the hardware details and decide how to do it.
