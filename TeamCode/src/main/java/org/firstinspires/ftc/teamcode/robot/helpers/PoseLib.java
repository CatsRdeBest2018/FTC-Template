package org.firstinspires.ftc.teamcode.robot.helpers;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

public final class PoseLib {
    private PoseLib() { }

    public static Pose pose(double x, double y, double headingDegrees) {
        PoseFactory field = PoseFactory.degrees();
        if (Alliance.get() == Alliance.Color.BLUE) {
            field = field.mirrorX(72).mirrorY(72);
        }
        return field.of(x, y, headingDegrees);
    }
}
