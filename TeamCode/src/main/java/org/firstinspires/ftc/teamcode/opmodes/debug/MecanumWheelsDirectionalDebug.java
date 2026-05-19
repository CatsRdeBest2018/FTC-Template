package org.firstinspires.ftc.teamcode.opmodes.debug;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Wheel Debug", group = "Debug")
public class MecanumWheelsDirectionalDebug extends OpMode {
    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;

    @Override
    public void init() {
        frontLeft = hardwareMap.get(DcMotorEx.class, "fl");
        frontRight = hardwareMap.get(DcMotorEx.class, "fr");
        backLeft = hardwareMap.get(DcMotorEx.class, "bl");
        backRight = hardwareMap.get(DcMotorEx.class, "br");

        frontLeft.setZeroPowerBehavior(BRAKE);
        frontRight.setZeroPowerBehavior(BRAKE);
        backLeft.setZeroPowerBehavior(BRAKE);
        backRight.setZeroPowerBehavior(BRAKE);

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        backRight.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addLine("Wheel Debug Ready");
        telemetry.update();

    }

    @Override
    public void loop() {

        if (gamepad1.x) {
            frontLeft.setPower(1);
        } else {
            frontLeft.setPower(0);
        }
        if (gamepad1.y) {
            frontRight.setPower(1);
        } else {
            frontRight.setPower(0);
        }
        if (gamepad1.a) {
            backLeft.setPower(1);
        } else {
            backLeft.setPower(0);
        }
        if (gamepad1.b) {
            backRight.setPower(1);
        } else {
            backRight.setPower(0);
        }

        telemetry.addData("X", "Front Left");
        telemetry.addData("Y", "Front Right");
        telemetry.addData("A", "Back Left");
        telemetry.addData("B", "Back Right");

        telemetry.update();
    }
}