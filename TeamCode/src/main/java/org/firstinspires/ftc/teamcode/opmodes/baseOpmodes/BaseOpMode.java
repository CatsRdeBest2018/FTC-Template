package org.firstinspires.ftc.teamcode.opmodes.baseOpmodes;

import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;
import org.firstinspires.ftc.teamcode.util.RobotTelemetry;

public abstract class BaseOpMode extends OpMode {
    public Robot r;
    protected RobotTelemetry telemetryManager;
    protected boolean hasStarted;
    protected abstract Alliance.Color getAlliance();

    @Override public void init() {
        hasStarted = false;
        Alliance.set(getAlliance());
        Scheduler.reset();
        telemetryManager = RobotTelemetry.begin(telemetry);
        r = new Robot(hardwareMap);
    }

    @Override public void start() {
        hasStarted = true;
    }

    @Override public void loop() {
        Scheduler.execute();
        r.update();
        sendTelemetry();
    }

    protected void sendTelemetry() {
        telemetryManager.setRobotPose(r.follower.pose());
        telemetryManager.addData("Alliance", Alliance.get());
        telemetryManager.update(telemetry);
    }

    @Override public void stop() {
        hasStarted = false;
        Scheduler.reset();
        r.stop();
    }
}
