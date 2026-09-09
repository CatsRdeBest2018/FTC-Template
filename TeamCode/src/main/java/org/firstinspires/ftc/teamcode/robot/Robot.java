package org.firstinspires.ftc.teamcode.robot;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.robot.hardware.HardwareNames;
import org.firstinspires.ftc.teamcode.robot.subsystems.LimelightController;

public class Robot {
    public final Follower follower;
    public final RobotMacros macros;
    public DcMotorEx frontLeft;
    public DcMotorEx frontRight;
    public DcMotorEx backLeft;
    public DcMotorEx backRight;

    public LimelightController limelightController;

    public Robot(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(DcMotorEx.class, HardwareNames.FRONT_LEFT_DRIVE);
        frontRight = hardwareMap.get(DcMotorEx.class, HardwareNames.FRONT_RIGHT_DRIVE);
        backLeft = hardwareMap.get(DcMotorEx.class, HardwareNames.BACK_LEFT_DRIVE);
        backRight = hardwareMap.get(DcMotorEx.class, HardwareNames.BACK_RIGHT_DRIVE);

        frontLeft.setZeroPowerBehavior(BRAKE);
        frontRight.setZeroPowerBehavior(BRAKE);
        backLeft.setZeroPowerBehavior(BRAKE);
        backRight.setZeroPowerBehavior(BRAKE);

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        follower = Constants.createFollower(hardwareMap);
        limelightController = new LimelightController(hardwareMap);
        macros = new RobotMacros();
    }

    public void motorDriveXYVectors(double x, double y, double rotation) {
        double frontLeftPower = y + x + rotation;
        double backLeftPower = y - x + rotation;
        double frontRightPower = y - x - rotation;
        double backRightPower = y + x - rotation;

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);
    }
    public void tick(Gamepad gamepad1, Gamepad gamepad2) {
        boolean driverWantsControl =
                Math.abs(gamepad1.left_stick_x) > 0.1 ||
                Math.abs(gamepad1.left_stick_y) > 0.1 ||
                Math.abs(gamepad1.right_stick_x) > 0.1;

        if (driverWantsControl) {
            macros.stop();
        }

        follower.update();
        limelightController.update();
    }

    public void stop() {
        follower.breakFollowing();
        macros.stop();
        limelightController.stop();
    }

    public class RobotMacros {
        public void driveToPos(Pose destination) {driveToPos(destination, 1.0, 1.0);}
        public void driveToPos(Pose destination, double drive_power, double position_tolerance) {
            Pose current = follower.getPose();
            Pose start = new Pose(current.getX(), current.getY(), current.getHeading());

            follower.setMaxPowerScaling(drive_power);

            if (Math.hypot(destination.getX() - start.getX(), destination.getY() - start.getY())
                    < position_tolerance) {
                follower.turnTo(destination.getHeading());
            } else {
                PathChain path = follower.pathBuilder()
                        .addPath(new BezierLine(start, destination))
                        .setLinearHeadingInterpolation(start.getHeading(), destination.getHeading())
                        .build();
                follower.followPath(path, true);
            }
        }

        public void stop() {
            follower.breakFollowing();
            follower.startTeleopDrive();
        }
    }
}
