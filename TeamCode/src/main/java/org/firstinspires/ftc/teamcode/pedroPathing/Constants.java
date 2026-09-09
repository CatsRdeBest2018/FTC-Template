package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.robot.hardware.HardwareNames;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            // Existing robot-specific measurements and tuned coefficients.
            .mass(5.761)
            .headingPIDFCoefficients(new PIDFCoefficients(1.25, 0, 0.1, 0.02))
            .translationalPIDFCoefficients(new PIDFCoefficients(0.1,0,0.01,0.01))
            .forwardZeroPowerAcceleration(-41.8504909065)
            .lateralZeroPowerAcceleration(-66.6475137966)

            // Pedro 2.1.2 defaults below: starting points, not tuned for this robot.
            // Drive coefficient order: P, I, D, derivative filter T, F.
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.025, 0, 0.00001, 0.6, 0.01))
            .centripetalScaling(0.0005)

            // Optional small-error controllers. PIDF coefficient order: P, I, D, F.
            .secondaryTranslationalPIDFCoefficients(new PIDFCoefficients(0.3, 0, 0.01, 0.015))
            .secondaryHeadingPIDFCoefficients(new PIDFCoefficients(5.0, 0, 0.08, 0.01))
            .secondaryDrivePIDFCoefficients(new FilteredPIDFCoefficients(0.02, 0, 0.000005, 0.6, 0.01))
            .translationalPIDFSwitch(3.0)
            .headingPIDFSwitch(Math.toRadians(9))
            .drivePIDFSwitch(20.0)

            // Keep these AFTER the secondary coefficients: their setters enable dual PIDF.
            .useSecondaryTranslationalPIDF(false)
            .useSecondaryHeadingPIDF(false)
            .useSecondaryDrivePIDF(false)

            // Optional filtering and hold-point behavior; leave at defaults initially.
            .driveKalmanFilterModelCovariance(6.0)
            .driveKalmanFilterDataCovariance(1.0)
            .holdPointTranslationalScaling(0.45)
            .holdPointHeadingScaling(0.35)
            .turnHeadingErrorThreshold(0.01)
            .automaticHoldEnd(true);

    // Expanded from the four-argument constructor without changing its effective values.
    public static PathConstraints pathConstraints = new PathConstraints(
            0.99,  // Path progress (t-value) threshold
            0.1,   // End velocity constraint
            0.1,   // End translational error constraint
            0.007, // End heading error constraint, radians
            100,   // End timeout, milliseconds
            1.0,   // Braking strength
            10,    // Bezier curve search limit
            1.0    // Braking start (global deceleration)
    );

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .pathConstraints(pathConstraints)
                .build();
    }

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName(HardwareNames.FRONT_RIGHT_DRIVE)
            .rightRearMotorName(HardwareNames.BACK_RIGHT_DRIVE)
            .leftRearMotorName(HardwareNames.BACK_LEFT_DRIVE)
            .leftFrontMotorName(HardwareNames.FRONT_LEFT_DRIVE)
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(55.0819047622)
            .yVelocity(45.2136560155)

            .useBrakeModeInTeleOp(true);

    public static PinpointConstants localizerConstants = new PinpointConstants()
            .forwardPodY(0)
            .strafePodX(0.5)
            .distanceUnit(DistanceUnit.INCH)
            .hardwareMapName(HardwareNames.PINPOINT)
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD);
}
