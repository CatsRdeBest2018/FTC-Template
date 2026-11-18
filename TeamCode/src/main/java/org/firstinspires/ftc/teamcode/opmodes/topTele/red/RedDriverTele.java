package org.firstinspires.ftc.teamcode.opmodes.topTele.red;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.opmodes.routines.tele.DriverTele;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;

@TeleOp(name = "Red Driver Tele", group = "Template")
public final class RedDriverTele extends DriverTele {
    @Override protected Alliance.Color getAlliance() { return Alliance.Color.RED; }

    @Override protected Pose getStartingPose() {
        // Used when there is no saved pose from a RED autonomous run.
        return PoseFactory.degrees().of(0, 0, 0);
    }
}
