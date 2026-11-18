package org.firstinspires.ftc.teamcode.robot.helpers;

import java.util.Objects;

/** Selected by the red/blue OpMode before the robot or routine is initialized. */
public final class Alliance {
    public enum Color { BLUE, RED }

    private static Color currentAlliance;

    private Alliance() { }

    public static Color get() {
        return Objects.requireNonNull(currentAlliance, "Select an alliance before building poses");
    }

    public static void set(Color alliance) {
        currentAlliance = Objects.requireNonNull(alliance, "alliance");
    }
}
