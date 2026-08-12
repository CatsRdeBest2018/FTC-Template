package org.firstinspires.ftc.teamcode.opmodes.debug;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
@TeleOp(name = "ControllerDebugger", group = "Debug")
public class ControllerDebugger extends OpMode {

    @Override
    public void init() {
        telemetry.addLine("Init");
    }

    @Override
    public void loop() {
        telemetry.addLine("Gamepad 1");
        telemetry.addData("gamepad1.a", gamepad1.a);
        telemetry.addData("gamepad1.b", gamepad1.b);
        telemetry.addData("gamepad1.x", gamepad1.x);
        telemetry.addData("gamepad1.y", gamepad1.y);
        telemetry.addLine("-------------");
        telemetry.addData("gamepad1.left_stick_y", gamepad1.left_stick_y);
        telemetry.addData("gamepad1.left_stick_x", gamepad1.left_stick_x);
        telemetry.addData("gamepad1.right_stick_y", gamepad1.right_stick_y);
        telemetry.addData("gamepad1.right_stick_x", gamepad1.right_stick_x);
        telemetry.addLine("-------------");
        telemetry.addData("gamepad1.dpad_left", gamepad1.dpad_left);
        telemetry.addData("gamepad1.dpad_up", gamepad1.dpad_up);
        telemetry.addData("gamepad1.dpad_right", gamepad1.dpad_right);
        telemetry.addData("gamepad1.dpad_down", gamepad1.dpad_down);
        telemetry.addLine("-------------");

        telemetry.update();
    }
}