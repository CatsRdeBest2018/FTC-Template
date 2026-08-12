package org.firstinspires.ftc.teamcode.notes;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.Servo;

import java.security.ProtectionDomain;

@TeleOp(name = "SampleTele", group = "Samples")
public class SampleTele extends OpMode {

    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;
    public DcMotorEx rightHang, leftHang;
    public DcMotorEx spinners;

   //slides (adarsh+emad)
    public DcMotorEx slides;
    int setPos = 0;
    //multiplier for gamepad input
    public static int slideSensitivity = 5;
    //max slide pos, (0 is the min)
    public static int max = 1200;

    //adarsh
    public static double clawPosOpen = 1;
    public static double clawPosClose = 0.55;
    public boolean clawOpen = true;



    //cynthia + vedant
    public Servo claw, tilt, plane;
    public Servo leftSlide, rightSlide;
    public Servo rightShove, leftShove;
    NormalizedColorSensor colorSensor;
    NormalizedColorSensor colorSensor2;

    public static double tiltOut = 0.83;
    public static double tiltIn = 0;


    @Override
    public void init() {

        //cynthia + vedant
        //drive
        frontLeft = hardwareMap.get(DcMotorEx.class, "motorFrontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "motorFrontRight");
        backLeft = hardwareMap.get(DcMotorEx.class, "motorBackLeft");
        backRight = hardwareMap.get(DcMotorEx.class, "motorBackRight");

        frontLeft.setZeroPowerBehavior(BRAKE);
        frontRight.setZeroPowerBehavior(BRAKE);
        backLeft.setZeroPowerBehavior(BRAKE);
        backRight.setZeroPowerBehavior(BRAKE);

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        //hang
        rightHang = hardwareMap.get(DcMotorEx.class, "rightHang");
        leftHang = hardwareMap.get(DcMotorEx.class, "leftHang");

        leftHang.setDirection(DcMotorEx.Direction.REVERSE);

        rightHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        rightHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightHang.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        leftHang.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);



        //spinner
        spinners = hardwareMap.get(DcMotorEx.class, "spinners");

        //slides init
        slides = hardwareMap.get(DcMotorEx.class, "slides");
        slides.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slides.setTargetPosition(setPos);
        slides.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        slides.setPower(0.5);

        //servos:
        claw = hardwareMap.get(Servo.class, "claw");
        tilt = hardwareMap.get(Servo.class, "tilt");
        leftShove = hardwareMap.get(Servo.class, "leftShove");
        plane = hardwareMap.get(Servo.class, "plane");

        //slides
        leftSlide = hardwareMap.get(Servo.class, "leftSlide");
        rightSlide = hardwareMap.get(Servo.class, "rightSlide");

        //shove
        rightShove = hardwareMap.get(Servo.class, "rightShove");
        leftShove = hardwareMap.get(Servo.class, "leftShove");

        //color sensors:
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "colorSensor");
        colorSensor2 = hardwareMap.get(NormalizedColorSensor.class, "colorSensor2");


        telemetry.addLine("Robot Initialized");
        telemetry.update();
    }

    @Override
    public void start() {
        tilt.setPosition(tiltIn);
        claw.setPosition(clawPosOpen);
        telemetry.addLine("TeleOp Started");
        telemetry.update();
    }


    @Override
    public void loop() {
        // DRIVE

        rightHang.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftHang.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // ethan
        double x = -gamepad1.left_stick_x;
        double y = gamepad1.left_stick_y;
        double rotation = gamepad1.right_stick_x;

        double frontLeftPower  = y + x + rotation;
        double backLeftPower   = y - x + rotation;
        double frontRightPower = y - x - rotation;
        double backRightPower  = y + x - rotation;

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);


        //intake and outtake
        if (gamepad2.a) {
            spinners.setPower(-1);
            leftShove.setPosition(tiltOut);
        }
        else if (gamepad2.b) {
            spinners.setPower(1);
        } else {
            spinners.setPower(0);
            leftShove.setPosition(tiltIn);
        }

        //adarsh + emad
        //slides
        //increment slide target based off of gamepad input
        setPos += Math.round(gamepad2.left_stick_y * slideSensitivity);
        //constrain set value to prevent death
        setPos = Math.min(max, setPos);
        setPos = Math.max(5, setPos);
        //set set value

        slides.setTargetPosition(setPos);


        //stopper
        //adarsh+cynthia
        if (gamepad2.xWasPressed() && clawOpen) {
            claw.setPosition(clawPosClose);
            clawOpen = false;
        }
        else if (gamepad2.xWasPressed() && !clawOpen) {
            claw.setPosition(clawPosOpen);
            clawOpen = true;
        }
        //adarsh+emad
        if (gamepad2.left_bumper && gamepad2.right_bumper) {
            leftHang.setTargetPosition(1350);
            rightHang.setTargetPosition(1350);
            leftHang.setPower(0.5);
            rightHang.setPower(0.5);
            leftHang.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            rightHang.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        }

    }


    @Override
    public void stop() {
        telemetry.addLine("TeleOp Stopped");
        telemetry.update();
    }
}