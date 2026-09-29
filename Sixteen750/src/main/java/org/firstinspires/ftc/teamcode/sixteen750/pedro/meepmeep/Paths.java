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
    Poses.Push12ParkPoses p = new Poses.Push12ParkPoses();

    public static Follower follower;
    public Path Launch1ToGardenPreInt;
    public Path GardenPreIntToGardenInt;
    public Path GardenIntToLaunch2;
    public Path Launch2ToPark;

    public Path StartToPartnerPark() {
        return line(sp.Start, p.PartnerPark).constant(p.PartnerParkHead);
    }

    public Path PartnerParkToLaunch1() {
        return line(p.PartnerPark, p.Launch1).linear(p.PartnerParkHead, p.Launch1Head);
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

    public Path Launch2ToPark() {
        return line(p.Launch2, p.Park).constant(p.ParkHead);
    }
}
