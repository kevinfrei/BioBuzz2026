package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.OctoQuadTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;
import org.firstinspires.ftc.teamcode.pedro.procedures.TwoWheelTuner;
import org.firstinspires.ftc.teamcode.twenty403.PedroConstants;

public class Tuning {

    // Tuners go here
    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    @Tuner
    public static Procedure octoquadTuner() {
        return new OctoQuadTuner();
    }

    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(
            hardwareMap -> new OctoQuadLocalizer(hardwareMap, PedroConstants.localizerConfig),
            hardwareMap -> new Mecanum(hardwareMap, PedroConstants.drivetrainConfig)
        );
    }

    @Tuner
    public static Procedure tests() {
        return new Tests(
            hardwareMap -> new Mecanum(hardwareMap, PedroConstants.drivetrainConfig),
            hardwareMap -> new OctoQuadLocalizer(hardwareMap, PedroConstants.localizerConfig),
            () -> new Foresight(PedroConstants.foresightConfig)
        );
    }
}
