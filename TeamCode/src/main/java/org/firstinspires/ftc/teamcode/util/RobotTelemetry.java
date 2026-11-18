package org.firstinspires.ftc.teamcode.util;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared per-loop readings sent to Panels and Driver Station. */
public final class RobotTelemetry {
    private static final RobotTelemetry INSTANCE = new RobotTelemetry();
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final List<String> lines = new ArrayList<>();
    private Pose robotPose;

    private RobotTelemetry() { }

    public static RobotTelemetry getInstance() {
        return INSTANCE;
    }

    /** Call once in OpMode.init(), before constructing subsystems. */
    public static RobotTelemetry begin(Telemetry driverStation) {
        synchronized (INSTANCE) {
            INSTANCE.values.clear();
            INSTANCE.lines.clear();
            INSTANCE.robotPose = null;
        }
        PanelsTelemetry.INSTANCE.getTelemetry().getLines().clear();
        driverStation.clearAll();
        return INSTANCE;
    }

    public synchronized void addData(String caption, Object value) {
        if (values.size() >= 200) return;
        // Multiple hubs/devices can use the same caption in one report.
        String key = caption;
        int index = 2;
        while (values.containsKey(key)) key = caption + " (" + index++ + ")";
        values.put(key, value);
    }

    public synchronized void addLine(String line) {
        // Keep accidental missing update() calls from growing memory indefinitely.
        if (lines.size() < 100) lines.add(line);
    }

    /** Include the current Pedro pose in numeric telemetry; the localizer draws the field. */
    public synchronized void setRobotPose(Pose pose) {
        robotPose = pose;
    }

    /** Flush once at the end of a loop, after subsystems have added their readings. */
    public synchronized void update(Telemetry driverStation) {
        TelemetryManager panels = PanelsTelemetry.INSTANCE.getTelemetry();
        if (robotPose != null) {
            addData("Pedro X", robotPose.x());
            addData("Pedro Y", robotPose.y());
            addData("Heading degrees", Math.toDegrees(robotPose.heading()));

        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            panels.addData(entry.getKey(), entry.getValue());
        }
        for (String line : lines) {
            panels.addLine(line);
        }
        values.clear();
        lines.clear();
        robotPose = null;
        panels.update(driverStation);
    }
}
