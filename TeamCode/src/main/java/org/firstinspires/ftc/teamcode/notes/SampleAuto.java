package org.firstinspires.ftc.teamcode.notes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.robot.Robot;

@Autonomous(name = "Sample Auto", group = "Auto")
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
        DONE
    }

    // Internal sequence state
    private SequenceSteps SequenceStep = SequenceSteps.DRIVE_FORWARD;

    private void runSequenceState() {
        switch (SequenceStep) {
            case DRIVE_FORWARD:

                robot.simpleDriveController.forward(0.5, 1000);

                if (!robot.simpleDriveController.isBusy()) {
                    SequenceStep = SequenceSteps.STRAFE_RIGHT;
                }
                break;

            case STRAFE_RIGHT:
                robot.simpleDriveController.strafeRight(0.5, 750);
                
                if (!robot.simpleDriveController.isBusy()) {
                    SequenceStep = SequenceSteps.DONE;
                }
                break;
            case DONE:
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
        }

        telemetry.addData("Auto State", currentAutoState);

        telemetry.update();
    }
}