package org.firstinspires.ftc.teamcode.opmodes.routines.tele;

import org.firstinspires.ftc.teamcode.opmodes.baseOpmodes.BaseTele;

public abstract class DriverTele extends BaseTele {

    @Override
    public void loop() {
        r.follower.manual(-gamepad1.left_stick_y, -gamepad1.left_stick_x,
                -gamepad1.right_stick_x);
        super.loop();
    }
}
