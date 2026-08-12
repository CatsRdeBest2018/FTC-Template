package org.firstinspires.ftc.teamcode.opmodes.debug;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
//everyone but primarily ethan
@Configurable
@TeleOp(name = "Servo Tester", group = "Debug")
public class ServoTester extends OpMode {

    public static String SERVO_NAME = "servo";
    public static double SERVO_POSITION = 0.5;
    public static boolean ENABLE_SERVO = false;

    private Servo servo;
    private String currentServoName = "";

    public DcMotorEx rightHang, leftHang;


    @Override
    public void init() {
        rightHang = hardwareMap.get(DcMotorEx.class, "rightHang");
        leftHang = hardwareMap.get(DcMotorEx.class, "leftHang");

        leftHang.setDirection(DcMotorEx.Direction.REVERSE);

        rightHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        rightHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        telemetry.addLine("Enter servo name in Dashboard");
        telemetry.addLine("Set ENABLE_SERVO = true when ready");
        telemetry.update();
    }

    @Override
    public void loop() {

        if (!currentServoName.equals(SERVO_NAME)) {
            try {
                servo = hardwareMap.get(Servo.class, SERVO_NAME);
                currentServoName = SERVO_NAME;

                telemetry.addData("Loaded Servo", SERVO_NAME);
            } catch (Exception e) {
                servo = null;
                currentServoName = "";

                telemetry.addData("Servo Error", "Could not find: " + SERVO_NAME);
            }
        }

        if (ENABLE_SERVO) {
            if (servo == null) {
                telemetry.addLine("Servo is null");
                return;
            }
            servo.setPosition(SERVO_POSITION);

            telemetry.addData("Servo", SERVO_NAME);
            telemetry.addData("Position", SERVO_POSITION);
        } else {
            telemetry.addLine("Servo disabled");
        }


        telemetry.addData("Right Hang Pos", rightHang.getCurrentPosition());
        telemetry.addData("Left Hang Pos", leftHang.getCurrentPosition());

        telemetry.update();
    }
}