package org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep;

import static com.pedropathing.api.Paths.*;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.curves.Line;
import com.pedropathing.paths.curves.bezier.BezierCurve;
import com.pedropathing.paths.interpolator.Interpolator;

@Configurable
public class Paths {

    Poses.StartPoses sp = new Poses.StartPoses();

    public static Follower follower;
    public Path Launch1ToGardenPreInt;
    public Path GardenPreIntToGardenInt;
    public Path GardenIntToLaunch2;
    public Path Launch2ToPark;
    Poses.Push1TipParkPoses p = new Poses.Push1TipParkPoses();

    public Path StartToPartnerPark() {
        return line(sp.Start, p.PartnerPark).constant(p.PartnerParkHead);
    }

    public Path PartnerParkToLaunch1() {
        return line(p.PartnerPark, p.Launch1).constant(p.Launch1Head);
    }

    public Path Launch1ToGardenPreInt() {
        return line(p.Launch1, p.GardenPreInt).constant(p.GardenIntHead);
    }

    public Path GardenPreIntToGardenInt() {
        return line(p.GardenPreInt, p.GardenInt).constant(p.GardenIntHead);
    }

    public Path GardenIntToLaunch2() {
        return curve(p.GardenInt, p.Launch2Ctrl1, p.Launch2Ctrl2, p.Launch2).constant(
            p.Launch2Head
        );
    }

    public Path GardenIntToLaunch1() {
        return line(p.GardenInt, p.Launch1).constant(p.Launch1Head);
    }

    public Path Launch2ToParkL() {
        return line(p.Launch2, p.ParkL).constant(p.ParkHeadL);
    }

    public Path Launch1ToParkR() {
        return curve(p.Launch1, p.ParkRCtrl, p.ParkR).constant(p.ParkHeadR);
    }
    Poses.Solo1TipParkPoses p1 = new Poses.Solo1TipParkPoses();
    public Path SStartToLaunch() {
        return line(sp.SideStart, p1.Launch).constant(p1.LaunchHead);
    }
    public Path SLaunchToGardenPreInt() {
        return line(p1.Launch, p1.GardenPreInt).constant(p1.GardenIntHead);
    }
    public Path SGardenPreIntToGardenInt() {
        return line(p1.GardenPreInt, p1.GardenInt).constant(p1.GardenIntHead);
    }
    public Path SGardenIntToLaunch() {
        return line(p1.GardenInt, p1.Launch).constant(p1.LaunchHead);
    }
    public Path SLaunchToPark() {
        return curve(p1.Launch, p1.ParkCtrl, p1.Park).constant(p1.ParkHead);
    }
}
