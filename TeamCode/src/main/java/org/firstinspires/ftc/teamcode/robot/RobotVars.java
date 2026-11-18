package org.firstinspires.ftc.teamcode.robot;

import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;

/** Auto-to-TeleOp pose handoff for this app session only. */
public final class RobotVars {
    private static Pose lastPose;
    private static Alliance.Color lastAlliance;

    private RobotVars() { }

    public static void clearLastPose() {
        lastPose = null;
        lastAlliance = null;
    }

    public static void setLastPose(Pose pose, Alliance.Color alliance) {
        lastPose = pose;
        lastAlliance = alliance;
    }

    public static Pose takeLastPose(Alliance.Color alliance) {
        Pose result = lastAlliance == alliance ? lastPose : null;
        clearLastPose();
        return result;
    }
}
