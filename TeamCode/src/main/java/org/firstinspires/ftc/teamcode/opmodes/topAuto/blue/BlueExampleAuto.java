package org.firstinspires.ftc.teamcode.opmodes.topAuto.blue;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.firstinspires.ftc.teamcode.opmodes.routines.autos.ExampleAuto;
import org.firstinspires.ftc.teamcode.robot.helpers.Alliance;

@Autonomous(name = "Blue Example Auto", group = "Template")
public final class BlueExampleAuto extends ExampleAuto {
    @Override protected Alliance.Color getAlliance() { return Alliance.Color.BLUE; }
}
