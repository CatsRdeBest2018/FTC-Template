package org.firstinspires.ftc.teamcode.robot;

import static com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior.BRAKE;
import static com.pedropathing.api.Paths.line;
import static com.pedropathing.ivy.commands.Commands.lazy;

import com.pedropathing.follower.Follower;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.CommandBuilder;
import com.pedropathing.ivy.behaviors.EndCondition;
import com.pedropathing.ivy.pedro.PedroCommands;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedro.Constants;
import org.firstinspires.ftc.teamcode.robot.hardware.HardwareNames;
import org.firstinspires.ftc.teamcode.robot.subsystems.LimelightController;

public class Robot {
    public final Follower follower;
    public final Commands commands;
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

        frontLeft.setDirection(DcMotorEx.Direction.FORWARD);
        frontRight.setDirection(DcMotorEx.Direction.FORWARD);
        backLeft.setDirection(DcMotorEx.Direction.FORWARD);
        backRight.setDirection(DcMotorEx.Direction.FORWARD);

        frontLeft.setZeroPowerBehavior(BRAKE);
        frontRight.setZeroPowerBehavior(BRAKE);
        backLeft.setZeroPowerBehavior(BRAKE);
        backRight.setZeroPowerBehavior(BRAKE);

        follower = Constants.createFollower(hardwareMap);
        commands = new Commands();
    }

    public void motorDriveXYVectors(double x, double y, double rotation) {
        double frontLeftPower = y + x + rotation;
        double backLeftPower = y - x + rotation;
        double frontRightPower = y - x - rotation;
        double backRightPower = y + x - rotation;

        double scale = Math.max(1.0, Math.abs(y) + Math.abs(x) + Math.abs(rotation));
        frontLeft.setPower(frontLeftPower / scale);
        backLeft.setPower(backLeftPower / scale);
        frontRight.setPower(frontRightPower / scale);
        backRight.setPower(backRightPower / scale);
    }

    public void update() {
        follower.update();
        updateSubsystems();
    }

    public void updateSubsystems() {
        if (limelightController != null) limelightController.update();
    }

    public void stop() {
        follower.stop();
        follower.drivetrain.stop();
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
        if (limelightController != null) limelightController.stop();
    }

    public final class Commands {
        public Command driveToPos(Pose destination) {
            return driveToPos(destination, 1.0, 1.0);
        }

        public Command driveToPos(Pose destination, double drivePower, double positionTolerance) {
            if (destination == null || !Double.isFinite(drivePower)
                    || drivePower <= 0 || drivePower > 1
                    || !Double.isFinite(positionTolerance) || positionTolerance < 0) {
                throw new IllegalArgumentException("Provide a destination, power in (0, 1], and nonnegative tolerance");
            }
            return lazy(() -> {
                Pose start = follower.pose();
                if (start.distance(destination) <= positionTolerance) {
                    return holdPosition(destination);
                }
                Path path = line(start, destination)
                        .linear(start, destination)
                        .with(Constants.foresightConfig.maxPathSpeed.at(drivePower));
                follower.holdEnd.set(true);
                return followPath(path);
            }).requiring(follower);
        }

        public Command followPath(Path path) {
            return stopOnInterrupt(PedroCommands.follow(follower, path).requiring(follower));
        }

        public Command holdPosition(Pose destination) {
            return stopOnInterrupt(PedroCommands.hold(follower, destination).requiring(follower));
        }

        private CommandBuilder stopOnInterrupt(CommandBuilder command) {
            return command.setEnd(reason -> {
                if (reason != EndCondition.NATURALLY) {
                    follower.stop();
                    follower.drivetrain.stop();
                }
            });
        }
    }
}
