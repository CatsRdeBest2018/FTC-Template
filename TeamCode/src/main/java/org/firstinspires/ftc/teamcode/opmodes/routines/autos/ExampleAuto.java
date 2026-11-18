package org.firstinspires.ftc.teamcode.opmodes.routines.autos;

import static com.pedropathing.ivy.commands.Commands.instant;
import static com.pedropathing.ivy.groups.Groups.sequential;

import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.opmodes.baseOpmodes.BaseAuto;
import org.firstinspires.ftc.teamcode.robot.helpers.PoseLib;

public abstract class ExampleAuto extends BaseAuto {
    @Override protected Pose getStartingPose() {
        // Placeholder RED-side pose; BLUE is rotated around the field center.
        return PoseLib.pose(12, 12, 0);
    }

    @Override protected Command buildRoutine() {
        return sequential(
                instant(() -> telemetryManager.addLine("Example auto started; add your routine here"))
                // Add commands here, for example:
                // , r.commands.driveToPos(PoseLib.pose(36, 12, 0))
        );
    }
}
