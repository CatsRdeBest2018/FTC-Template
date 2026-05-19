package org.firstinspires.ftc.teamcode.notes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.robot.Robot;

@Autonomous(name = "Sample Auto", group = "Samples")
public class SampleAuto extends OpMode {

    private Robot robot;

    public enum AutoState {
        SequenceState
    }

    // Main auto state
    private AutoState currentAutoState = AutoState.SequenceState;


    public enum SequenceSteps {
        DRIVE_FORWARD,
        STRAFE_RIGHT,
        DRIVE_BACKWARD,
        STRAFE_LEFT,
        DONE
    }

    // Internal sequence state
    private SequenceSteps SequenceStep = SequenceSteps.DRIVE_FORWARD;

    private void runSequenceState() {
        switch (SequenceStep) {
            case DRIVE_FORWARD:
                robot.simpleDriveController.forward(0.5, 750);

                if (!robot.simpleDriveController.isBusy()) {
                    robot.simpleDriveController.bypass = true;
                    SequenceStep = SequenceSteps.STRAFE_RIGHT;
                }
                break;

            case STRAFE_RIGHT:
                robot.simpleDriveController.strafeRight(0.5, 750);
                
                if (!robot.simpleDriveController.isBusy()) {
                    robot.simpleDriveController.bypass = true;
                    SequenceStep = SequenceSteps.DRIVE_BACKWARD;
                }
                break;

            case DRIVE_BACKWARD:
                robot.simpleDriveController.backward(0.5, 750);

                if (!robot.simpleDriveController.isBusy()) {
                    robot.simpleDriveController.bypass = true;
                    SequenceStep = SequenceSteps.STRAFE_LEFT;
                }
                break;

            case STRAFE_LEFT:
                robot.simpleDriveController.strafeLeft(0.5, 750);

                if (!robot.simpleDriveController.isBusy()) {
                    robot.simpleDriveController.bypass = true;
                    SequenceStep = SequenceSteps.DRIVE_FORWARD;
                }
                break;
            case DONE:
                requestOpModeStop();
        }
    }

    @Override
    public void init() {
        robot = new Robot();
        robot.init(hardwareMap);

        telemetry.addLine("Auto Initialized");
        telemetry.update();
    }

    @Override
    public void start() {
    }

    @Override
    public void loop() {
        robot.tick();

        switch (currentAutoState) {
            case SequenceState:
                runSequenceState();
        }

        telemetry.addData("Auto State", currentAutoState);
        telemetry.addData("Current Sequence Step", SequenceStep);
        telemetry.update();
    }
}