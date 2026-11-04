package org.firstinspires.ftc.teamcode.twenty403.commands.auto;

import static com.pedropathing.api.Paths.*;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class AutoPaths {

    private final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(11.265, 87.872, 90);
    private final Pose point1 = poseFactory.of(55.5086, 11.1046, -90);
    private final Pose point1Control1 = poseFactory.of(16.565, 20.2398, 0);
    private final Pose point2 = poseFactory.of(9.6869, 8.8542, -90.7925);
    private final Pose point2Control1 = poseFactory.of(10.5098, 68.3378, 0);
    private final Pose point3Start = poseFactory.of(9.6869, 8.8542, -90);
    private final Pose point3 = poseFactory.of(58.4778, 11.5063, 90);
    private final Pose point3Control1 = poseFactory.of(26.9306, 53.832, 0);
    private final Pose point4 = poseFactory.of(14.0421, 93.099, 90);
    private final Pose point4Control1 = poseFactory.of(15.5412, 36.8921, 0);

    public Path pathDwayne() {
        return Paths.curve(start, point1Control1, point1).constant(point1);
    }

    public Path pathJimmy() {
        return Paths.curve(point1, point2Control1, point2).tangent();
    }

    public Path pathJohn() {
        return Paths.curve(point3Start, point3Control1, point3).linear(point3Start, point3);
    }

    public Path path_park() {
        return Paths.curve(point3, point4Control1, point4).constant(point4);
    }
}
