package org.firstinspires.ftc.teamcode.robot;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.hardware.Servo;

public class Robot {
    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;

    Limelight3A limelight;
    public LimelightController limelightController = new LimelightController();
    public SimpleDriveController simpleDriveController = new SimpleDriveController();

    public void init(HardwareMap hardwareMap) {

        // =========================
        // DRIVE
        // =========================

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


        // =========================
        // LIMELIGHT
        // =========================

        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(0);
        limelight.start();
    }

    public void motorDriveXYVectors(double x, double y, double rotation) {

        double frontLeftPower  = y + x + rotation;
        double backLeftPower   = y - x + rotation;
        double frontRightPower = y - x - rotation;
        double backRightPower  = y + x - rotation;

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);
    }

    public class LimelightController {

        private LLResult result;

        public void update() {
            result = limelight.getLatestResult();
        }

        public boolean hasTarget() {
            return result != null && result.isValid();
        }

        public double getTx() {
            if (hasTarget()) {
                return result.getTx();
            }
            return 0;
        }

        public double getTy() {
            if (hasTarget()) {
                return result.getTy();
            }
            return 0;
        }

        public double getDistanceMeters() {
            if (hasTarget()) {
                return result.getBotposeAvgDist();
            }
            return 0;
        }

        public double getDistanceInches() {
            return getDistanceMeters() * 39.3701;
        }

        public void setPipeline(int pipeline) {
            limelight.pipelineSwitch(pipeline);
        }

        public void start() {
            limelight.start();
        }

        public void stop() {
            limelight.stop();
        }
    }

    public class SimpleDriveController {

        private final ElapsedTime timer = new ElapsedTime();

        private boolean active = false;
        private double durationMs = 0;

        private double fl = 0;
        private double fr = 0;
        private double bl = 0;
        private double br = 0;


        public void tick() {

            if (!active) return;

            // Stop when timer expires
            if (timer.milliseconds() >= durationMs) {

                stop();

                return;
            }

            // Apply powers
            frontLeft.setPower(fl);
            frontRight.setPower(fr);
            backLeft.setPower(bl);
            backRight.setPower(br);
        }

        public void forward(double power, double timeMs) {

            startDrive(
                    power,
                    power,
                    power,
                    power,
                    timeMs
            );
        }

        public void backward(double power, double timeMs) {

            startDrive(
                    -power,
                    -power,
                    -power,
                    -power,
                    timeMs
            );
        }

        public void strafeRight(double power, double timeMs) {

            startDrive(
                    power,
                    -power,
                    -power,
                    power,
                    timeMs
            );
        }

        public void strafeLeft(double power, double timeMs) {

            startDrive(
                    -power,
                    power,
                    power,
                    -power,
                    timeMs
            );
        }

        private void startDrive(
                double frontLeftPower,
                double frontRightPower,
                double backLeftPower,
                double backRightPower,
                double timeMs
        ) {
            if (active) return;

            fl = frontLeftPower;
            fr = frontRightPower;
            bl = backLeftPower;
            br = backRightPower;

            durationMs = timeMs;

            timer.reset();
            active = true;
        }

        public void stop() {

            frontLeft.setPower(0);
            frontRight.setPower(0);
            backLeft.setPower(0);
            backRight.setPower(0);

            active = false;
        }

        public boolean isBusy() {
            return active;
        }
    }
    public void tick() {
        simpleDriveController.tick();
    }

}