package org.firstinspires.ftc.teamcode.util;

import com.bylazar.field.FieldManager;
import com.bylazar.field.PanelsField;
import com.pedropathing.math.Pose;

/** Shared Panels field output, independent of any OpMode's telemetry loop. */
public final class LiveFieldView {
    private LiveFieldView() { }

    public static synchronized void show(Pose pose) {
        if (pose == null || !Double.isFinite(pose.x()) || !Double.isFinite(pose.y())
                || !Double.isFinite(pose.heading())) return;

        FieldManager field = PanelsField.INSTANCE.getField();
        // Respect Panels' configured refresh rate instead of drawing at control-loop speed.
        if (!field.getShouldUpdateCanvas()) return;

        field.setOffsets(PanelsField.INSTANCE.getPresets().getPEDRO_PATHING());
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), "#3F51B5", 2);
        field.moveCursor(pose.x(), pose.y());
        field.circle(8);
        field.line(pose.x() + 8 * Math.cos(pose.heading()),
                pose.y() + 8 * Math.sin(pose.heading()));
        field.update();
    }
}
