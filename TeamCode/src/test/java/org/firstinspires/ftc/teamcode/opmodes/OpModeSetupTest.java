package org.firstinspires.ftc.teamcode.opmodes;

import static org.junit.Assert.*;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.math.Pose;

import org.firstinspires.ftc.teamcode.opmodes.baseOpmodes.BaseOpMode;
import org.firstinspires.ftc.teamcode.robot.RobotVars;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;
import org.firstinspires.ftc.teamcode.robot.helpers.PoseLib;
import org.junit.After;
import org.junit.Test;


public class OpModeSetupTest {
    private static class TestOpMode extends BaseOpMode {
        @Override protected Alliance.Color getAlliance() { return Alliance.Color.BLUE; }
    }

    @After public void cleanup() {
        Scheduler.reset();
        RobotVars.clearLastPose();
    }

    @Test public void stopClearsDirectlyScheduledCommands() {
        TestOpMode opMode = new TestOpMode();
        int[] starts = {0};
        int[] updates = {0};
        Command command = Command.build().setStart(() -> starts[0]++)
                .setExecute(() -> updates[0]++)
                .setDone(() -> false);
        assertEquals(0, starts[0]);
        opMode.start();
        command.schedule();
        assertEquals(1, starts[0]);
        Scheduler.execute();
        assertEquals(1, updates[0]);
        opMode.stop();
        assertFalse(command.isScheduled());
        Scheduler.execute();
        assertEquals(1, updates[0]);
    }

    @Test public void clearingPoseDiscardsThePreviousAuto() {
        RobotVars.setLastPose(new Pose(10, 20, 0), Alliance.Color.BLUE);
        RobotVars.clearLastPose();
        assertNull(RobotVars.takeLastPose(Alliance.Color.BLUE));
    }

    @Test public void poseHandoffIsConsumedOnceAndOnlyForTheSameAlliance() {
        Pose pose = new Pose(10, 20, 0.5);
        RobotVars.setLastPose(pose, Alliance.Color.BLUE);
        assertSame(pose, RobotVars.takeLastPose(Alliance.Color.BLUE));
        assertNull(RobotVars.takeLastPose(Alliance.Color.BLUE));
        RobotVars.setLastPose(pose, Alliance.Color.BLUE);
        assertNull(RobotVars.takeLastPose(Alliance.Color.RED));
        assertNull(RobotVars.takeLastPose(Alliance.Color.BLUE));
    }

    @Test public void bluePosesRotateRedCoordinatesAndSwitchingBackDoesNotDrift() {
        Alliance.set(Alliance.Color.RED);
        Pose red = PoseLib.pose(12, 24, 30);
        Alliance.set(Alliance.Color.BLUE);
        Pose blue = PoseLib.pose(12, 24, 30);
        assertEquals(132, blue.x(), 1e-9);
        assertEquals(120, blue.y(), 1e-9);
        assertEquals(-Math.cos(red.heading()), Math.cos(blue.heading()), 1e-9);
        assertEquals(-Math.sin(red.heading()), Math.sin(blue.heading()), 1e-9);
        Alliance.set(Alliance.Color.RED);
        Pose restored = PoseLib.pose(12, 24, 30);
        assertEquals(red.x(), restored.x(), 1e-9);
        assertEquals(red.y(), restored.y(), 1e-9);
        assertEquals(red.heading(), restored.heading(), 1e-9);
    }
}
