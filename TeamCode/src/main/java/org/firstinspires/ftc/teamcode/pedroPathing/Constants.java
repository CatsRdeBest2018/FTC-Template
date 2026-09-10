package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.robot.hardware.HardwareNames;

import java.util.OptionalDouble;

/** Pedro Pathing 3 configuration for this robot. */
public final class Constants {
    private Constants() {}

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("motorFrontLeft");
        c.frontRightName.set("motorFrontRight");
        c.backLeftName.set("motorBackLeft");
        c.backRightName.set("motorBackRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(0.13875467570747915);
        c.yPodOffset.set(-0.38842749407910926);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    public static final ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryForward = Controller.proportional(0.3); // NEW
        Controller secondaryForward = Controller.proportional(0.1); // NEW
        Controller primaryStrafe = Controller.proportional(0.3); // NEW
        Controller secondaryStrafe = Controller.proportional(0.1); // NEW

        c.forwardTranslational.set(
                Controller.piecewise(secondaryForward).put(2.5, primaryForward)); // NEW
        c.strafeTranslational.set(
                Controller.piecewise(secondaryStrafe).put(2.5, primaryStrafe)); // NEW
        c.headingFeedback.set(Controller.proportional(5.0)); // NEW
        c.headingStaticFF.set(Controller.staticFeedforward(0.0)); // NEW
        c.coast.set(Controller.proportionalFeedforward(0.01)); // NEW
        c.brake.set(Controller.proportionalFeedforward(0.01)); // NEW

        // Required placeholder braking models. AutoTune should replace them.
        c.linearBrakeCoefficients.set(Matrix.diag(0.1, 0.1)); // NEW
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.001, 0.001)); // NEW
        c.headingBrakeCoefficients.set(Vector2D.cartesian(0.05, 0.005)); // NEW

        // Robot measurements transferred from the Pedro 2 constants.
        c.maxAchievableForwardVelocity.set(55.0819047622);
        c.maxAchievableStrafeVelocity.set(45.2136560155);
        c.naturalForwardDeceleration.set(41.8504909065);
        c.naturalStrafeDeceleration.set(66.6475137966);

        c.holdPointTranslationalScaling.set(0.45);
        c.holdPointHeadingScaling.set(0.35);
        c.maxBrakingPower.set(0.2); // NEW
        c.maxAccelerationConstraint.set(ForesightConfig.Constraint.NONE); // NEW
        c.maxVelocityConstraint.set(ForesightConfig.Constraint.NONE); // NEW
        c.maxDecelerationConstraint.set(ForesightConfig.Constraint.NONE); // NEW
        c.maxPathSpeed.set(ForesightConfig.Constraint.NONE); // NEW
        c.maxDecelerationScale.set(ForesightConfig.Constraint.NONE); // NEW
        c.coastDownToVelocity.set(0.0); // NEW
        c.headingDeviationTolerance.set(Math.toRadians(11.25)); // NEW
        c.translationalDeviationTolerance.set(2.5); // NEW
        c.brakeAtEnd.set(true); // NEW
        c.pathSkip.set(true); // NEW
        c.headingDriveRatio.set(0.5); // NEW
        c.cosineScale.set(false); // NEW
        c.minCorrectionDistance.set(1e-3); // NEW
        c.parametricTConstraint.set(0.01); // 1 - previous 0.99 progress threshold
        c.velocityConstraint.set(0.1);
        c.translationalConstraint.set(0.1);
        c.headingConstraint.set(0.007);
        c.timeoutConstraint.set(100.0);
        c.brakeAggression.set(1.0);
    });

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new Follower(
                new PinpointLocalizer(hardwareMap, localizerConfig),
                new Mecanum(hardwareMap, drivetrainConfig),
                new Foresight(foresightConfig));
    }
}
