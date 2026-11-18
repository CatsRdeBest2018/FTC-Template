package org.firstinspires.ftc.teamcode.opmodes.baseOpmodes;

import com.pedropathing.ivy.Command;
import com.pedropathing.math.Pose;
import org.firstinspires.ftc.teamcode.robot.RobotVars;

import java.util.Objects;

public abstract class BaseAuto extends BaseOpMode {
    private Command routine;

    protected abstract Pose getStartingPose();
    protected abstract Command buildRoutine();

    @Override public void init() {
        RobotVars.clearLastPose();
        super.init();
        r.follower.setPose(getStartingPose());
        // Construct commands in INIT, but do not schedule them until START.
        routine = Objects.requireNonNull(buildRoutine(), "buildRoutine() must return a command");
    }

    @Override public void start() {
        super.start();
        routine.schedule();
    }

    @Override public void loop() {
        super.loop();
        savePose();
    }

    @Override public void stop() {
        try {
            savePose();
            if (routine != null) routine.cancel();
        } finally {
            super.stop();
        }
    }

    private void savePose() {
        if (hasStarted && r != null) {
            RobotVars.setLastPose(r.follower.pose(), getAlliance());
        }
    }
}
