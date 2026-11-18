package org.firstinspires.ftc.teamcode.opmodes.debug;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.opmodes.debug.util.HardwareTelemetry;
import org.firstinspires.ftc.teamcode.util.RobotTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

@Configurable
@TeleOp(name = "Servo Tester", group = "Debug")
public class ServoTester extends OpMode {
    private RobotTelemetry telemetryM;


    public static String SERVO_NAME = "servo";
    public static double SERVO_POSITION = 0.5;
    public static boolean ENABLE_SERVO = false;

    private Servo servo;
    private String currentServoName = "";

    @Override
    public void init() {
        telemetryM = RobotTelemetry.begin(telemetry);
        telemetryM.addLine("Enter servo name in Panels");
        telemetryM.addLine("Set ENABLE_SERVO = true when ready");
        telemetryM.update(telemetry);
    }

    @Override
    public void loop() {

        if (!currentServoName.equals(SERVO_NAME)) {
            try {
                servo = hardwareMap.get(Servo.class, SERVO_NAME);
                currentServoName = SERVO_NAME;

                telemetryM.addData("Loaded Servo", SERVO_NAME);
            } catch (Exception e) {
                servo = null;
                currentServoName = "";

                telemetryM.addData("Servo Error", "Could not find: " + SERVO_NAME);
            }
        }

        if (ENABLE_SERVO) {
            if (servo == null) {
                telemetryM.addData("Servo Error", "Could not find: " + SERVO_NAME);
            } else {
                double safePosition = Range.clip(SERVO_POSITION, 0.0, 1.0);
                servo.setPosition(safePosition);

                telemetryM.addData("Requested Position", SERVO_POSITION);
                telemetryM.addData("Applied Position", safePosition);
            }
        } else {
            telemetryM.addLine("Servo disabled");
        }

        if (servo != null) {
            HardwareTelemetry.addDeviceInfo(telemetryM, currentServoName, servo);
            telemetryM.addLine("Servo Readings");
            telemetryM.addData("Enabled", ENABLE_SERVO);
            telemetryM.addData("Position", servo.getPosition());
            telemetryM.addData("Direction", servo.getDirection());
        }

        telemetryM.update(telemetry);
    }
}
