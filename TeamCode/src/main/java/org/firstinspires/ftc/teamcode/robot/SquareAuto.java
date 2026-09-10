package org.firstinspires.ftc.teamcode.robot;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import static com.pedropathing.api.Paths.*;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.sequential;
import static com.pedropathing.ivy.pedro.PedroCommands.*;

@Autonomous(name = "SquareAuto")
public class SquareAuto extends OpMode {

    private Follower follower;

    private final PoseFactory p = PoseFactory.degrees();

    private final Pose start   = p.of(24, 24, 0);
    private final Pose corner1 = p.of(48, 24, 0);
    private final Pose corner2 = p.of(48, 48, 0);
    private final Pose corner3 = p.of(24, 48, 0);

    private Path side1() {
        return line(start, corner1).constant(start);
    }

    private Path side2() {
        return line(corner1, corner2).constant(start);
    }

    private Path side3() {
        return line(corner2, corner3).constant(start);
    }

    private Path side4() {
        return line(corner3, start).constant(start);
    }

    private Command autoRoutine() {
        return sequential(
                follow(follower, side1()),
                follow(follower, side2()),
                follow(follower, side3()),
                follow(follower, side4())
        );
    }

    @Override
    public void init() {
        Scheduler.reset();

        follower = Constants.createFollower(hardwareMap);

        follower.setPose(start);
        follower.update();
    }

    @Override
    public void start() {
        schedule(autoRoutine());
    }

    @Override
    public void loop() {
        follower.update();
        Scheduler.execute();

        telemetry.addData("X", follower.pose().x());
        telemetry.addData("Y", follower.pose().y());
        telemetry.addData(
                "Heading",
                Math.toDegrees(follower.pose().heading())
        );
        telemetry.addData("Follower Mode", follower.mode());

        telemetry.update();
    }
}