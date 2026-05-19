package org.firstinspires.ftc.teamcode.notes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.robot.Robot;

@TeleOp(name = "SampleTele", group = "Samples")
public class SampleTele extends OpMode {

    private Robot robot;

    @Override
    public void init() {
        robot = new Robot();
        robot.init(hardwareMap);

        telemetry.addLine("Robot Initialized");
        telemetry.update();
    }

    @Override
    public void start() {
        telemetry.addLine("TeleOp Started");
        telemetry.update();
    }

    @Override
    public void loop() {
        // DRIVE
        robot.motorDriveXYVectors(
                gamepad1.left_stick_x,
                -gamepad1.left_stick_y,
                gamepad1.right_stick_x
        );

        telemetry.update();
        robot.tick();
    }

    @Override
    public void stop() {
        telemetry.addLine("TeleOp Stopped");
        telemetry.update();
    }
}