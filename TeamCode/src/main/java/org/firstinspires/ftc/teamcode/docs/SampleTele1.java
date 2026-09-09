package org.firstinspires.ftc.teamcode.docs;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.Robot;
import org.firstinspires.ftc.teamcode.robot.config.RobotConstants;

@TeleOp(name = "SampleTele1", group = "Samples")
public class SampleTele1 extends OpMode {
    private Robot robot;
    private TelemetryManager telemetryM;

    @Override
    public void init() {
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
        robot = new Robot(hardwareMap);

        robot.follower.setStartingPose(new Pose(0, 0, 0));
        robot.follower.setMaxPower(RobotConstants.Drive.MAX_DRIVE_POWER);
    }

    @Override
    public void start() {
        robot.follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        double y = -gamepad1.left_stick_y;
        double x = -gamepad1.left_stick_x;
        double r = -gamepad1.right_stick_x;

        robot.follower.setTeleOpDrive(y, x, r, true);
        robot.tick(gamepad1,gamepad2);

        Pose pose = robot.follower.getPose();
        telemetryM.addData("X (in)", pose.getX());
        telemetryM.addData("Y (in)", pose.getY());
        telemetryM.addData("Heading (deg)", Math.toDegrees(pose.getHeading()));
        telemetryM.update(telemetry);
    }

    @Override
    public void stop() {
        if (robot != null) {
            robot.stop();
        }
    }
}
