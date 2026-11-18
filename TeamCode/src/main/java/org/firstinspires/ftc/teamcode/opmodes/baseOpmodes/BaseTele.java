package org.firstinspires.ftc.teamcode.opmodes.baseOpmodes;

import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.robot.RobotVars;

public abstract class BaseTele extends BaseOpMode {
    protected abstract Pose getStartingPose();

    @Override public void init() {
        super.init();
        Pose lastAutoPose = RobotVars.takeLastPose(getAlliance());
        r.follower.setPose(lastAutoPose != null ? lastAutoPose : getStartingPose());
        r.follower.manual(0, 0, 0);
    }

}
