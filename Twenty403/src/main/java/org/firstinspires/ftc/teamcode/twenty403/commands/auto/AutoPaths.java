package org.firstinspires.ftc.teamcode.twenty403.commands.auto;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

public class AutoPaths {

    private static final PoseFactory poseFactory = PoseFactory.degrees();

    private final Pose start = poseFactory.of(11.265, 87.872, 90);
    private final Pose point1 = poseFactory.of(57.663, 15.2174, -90);
    private final Pose point1Control1 = poseFactory.of(16.565, 20.2398, 0);
    private final Pose point2 = poseFactory.of(16.5416, 12.1836, -90);
    private final Pose point2Control1 = poseFactory.of(14.2309, 39.9398, 0);
    private final Pose point3Start = poseFactory.of(16.5416, 12.1836, -90);
    private final Pose point3 = poseFactory.of(57.6944, 14.8357, -90);
    private final Pose point3Control1 = poseFactory.of(26.9306, 53.832, 0);
    private final Pose point4 = poseFactory.of(14.0421, 93.099, -90);
    private final Pose point4Control1 = poseFactory.of(15.5412, 36.8921, 0);
    private final Pose northStart = poseFactory.of(10, 135.7, 0);
    private final Pose northToLaunchControl = poseFactory.of(22.91, 121.04, 0);
    private final Pose northPark = poseFactory.of(10, 115.11, 0);
    private final Pose northLaunch = poseFactory.of(59.46, 135.98, 90);

    public Path pathStartToGardenLaunch() {
        return Paths.curve(start, point1Control1, point1).constant(point1);
    }

    public Path pathGardenLaunchToGarden() {
        return Paths.curve(point1, point2Control1, point2).constant(point2);
    }

    public Path pathGardenToGardenLaunch() {
        return Paths.curve(point3Start, point3Control1, point3).linear(point3Start, point3);
    }

    public Path pathGardenLaunchToPark() {
        return Paths.curve(point3, point4Control1, point4).constant(point4);
    }

    public Path NorthStartToLaunch() {
        return Paths.curve(northStart, northToLaunchControl, northLaunch).linear(
            northStart,
            northLaunch
        );
    }

    public Path NorthLaunchToPark() {
        return Paths.curve(northLaunch, northPark).constant(northLaunch);
    }
}
