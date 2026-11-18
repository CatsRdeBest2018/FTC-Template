package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.localization.Localizer;
import com.pedropathing.localization.MotionState;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Twist;
import com.pedropathing.math.Velocity;
import java.util.Map;
import java.util.function.Consumer;
import org.firstinspires.ftc.teamcode.util.LiveFieldView;

/** Publishes the measured pose after localization, including tests without a follower. */
public final class FieldLocalizer implements Localizer {
    private final Localizer delegate;
    private final Consumer<Pose> display;

    public FieldLocalizer(Localizer delegate) {
        this(delegate, LiveFieldView::show);
    }

    FieldLocalizer(Localizer delegate, Consumer<Pose> display) {
        this.delegate = delegate;
        this.display = display;
    }

    @Override public void update() {
        delegate.update();
        display.accept(delegate.pose());
    }

    @Override public void setPose(Pose pose) {
        delegate.setPose(pose);
        display.accept(delegate.pose());
    }

    @Override public void setX(double x) {
        delegate.setX(x);
        display.accept(delegate.pose());
    }

    @Override public void setY(double y) {
        delegate.setY(y);
        display.accept(delegate.pose());
    }

    @Override public void setHeading(double heading) {
        delegate.setHeading(heading);
        display.accept(delegate.pose());
    }

    @Override public void reset() {
        delegate.reset();
        display.accept(delegate.pose());
    }

    @Override public Pose pose() { return delegate.pose(); }
    @Override public MotionState state() { return delegate.state(); }
    @Override public Velocity velocity() { return delegate.velocity(); }
    @Override public Twist twist() { return delegate.twist(); }
    @Override public Map<String, Object> debug() { return delegate.debug(); }
}
