package org.firstinspires.ftc.teamcode.opmodes.topAuto.red;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.opmodes.routines.autos.ExampleAuto;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;

@Autonomous(name = "Red Example Auto", group = "Template")
public final class RedExampleAuto extends ExampleAuto {
    @Override protected Alliance.Color getAlliance() { return Alliance.Color.RED; }
}
