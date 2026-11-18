package org.firstinspires.ftc.teamcode.opmodes.topTele.blue;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.opmodes.routines.tele.DriverTele;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;

@TeleOp(name = "Blue Driver Tele", group = "Template")
public final class BlueDriverTele extends DriverTele {
    @Override protected Alliance.Color getAlliance() { return Alliance.Color.BLUE; }

    @Override protected Pose getStartingPose() {
        // Used when there is no saved pose from a BLUE autonomous run.
        return PoseFactory.degrees().of(0, 0, 0);
    }
}
