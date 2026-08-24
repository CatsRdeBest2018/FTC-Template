package org.firstinspires.ftc.teamcode.robot.config;

/**
 * Predetermined values shared throughout the current robot's code.
 *
 * Add a nested class for each subsystem so constants remain organized as the
 * robot grows. Runtime state and live debug controls do not belong here.
 */
public final class RobotConstants {
    public static final class Drive {
        /** Maximum power used during normal driving. */
        public static final double MAX_DRIVE_POWER = 1.0;

        /** Conservative initial power for testing a robot on blocks. */
        public static final double DEFAULT_DEBUG_POWER = 0.20;

        /** Hard safety limit applied by RobotDebugger. */
        public static final double MAX_DEBUG_POWER = 0.40;

        private Drive() {}
    }

    /*
     * Add season-specific groups here as subsystems are created. Example:
     *
     * public static final class Slides {
     *     public static final int MIN_POSITION_TICKS = 0;
     *     public static final int MAX_POSITION_TICKS = 3000;
     *     public static final double KP = 0.01;
     *     public static final double KI = 0.0;
     *     public static final double KD = 0.0005;
     *
     *     private Slides() {}
     * }
     */

    private RobotConstants() {}
}
