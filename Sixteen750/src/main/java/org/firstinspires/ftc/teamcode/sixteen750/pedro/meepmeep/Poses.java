package org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;

@Configurable
public class Poses {

    public static Follower follower;

    @Configurable
    public static class StartPoses {

        public static Pose Start = new Pose(7.3, 64.5);

        public static Pose getStart() {
            return new Pose(7, 64, Math.toRadians(0));
        }
    }

    @Configurable
    public static class Push12ParkPoses {

        public static Pose PartnerPark = new Pose(11, 83);
        public static double PartnerParkHead = Math.toRadians(0);
        public static Pose Launch1 = new Pose(56, 18);
        public static double Launch1Head = Math.toRadians(90);
        public static Pose GardenPreInt = new Pose(7, 22);
        public static Pose GardenInt = new Pose(7, 9.2);
        public static double GardenIntHead = Math.toRadians(270);
        public static Pose Launch2 = new Pose(56, 125);
        public static double Launch2Head = Math.toRadians(270);
        public static Pose Launch2Ctrl1 = new Pose(48, 24);
        public static Pose Launch2Ctrl2 = new Pose(22, 132);
        public static Pose ParkL = new Pose(16, 119);
        public static Pose ParkR = new Pose(11, 94);
        public static Pose ParkRControl = new Pose(21, 51);
        public static double ParkHeadR = Math.toRadians(270);
        public static double ParkHeadL = Math.toRadians(180);
    }
}
