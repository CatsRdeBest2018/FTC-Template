package org.firstinspires.ftc.teamcode.docs;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.robot.Robot;

@TeleOp(name = "SampleTele", group = "Samples")
public class SampleTele extends OpMode {
    private Robot robot;

    @Override
    public void init() {
        robot = new Robot();

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
        double x = -gamepad1.left_stick_x;
        double y = gamepad1.left_stick_y;
        double rotation = gamepad1.right_stick_x;

        robot.motorDriveXYVectors(x,y,rotation);
    }


    @Override
    public void stop() {
        telemetry.addLine("TeleOp Stopped");
        telemetry.update();
    }
}