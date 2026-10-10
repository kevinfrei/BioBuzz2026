package org.firstinspires.ftc.teamcode.sixteen750;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.OctoQuadConfig;
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class PedroConstants {

    public static Follower create(HardwareMap h) {
        return new Follower(
            new OctoQuadLocalizer(h, localizerConfig),
            new Mecanum(h, drivetrainConfig),
            new Foresight(foresightConfig)
        );
    }

    // TODO: Run the tuners on the bot, fill these it with the results:
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("fl");
        c.frontRightName.set("fr");
        c.backLeftName.set("rl");
        c.backRightName.set("rr");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });
    // all this octoquad stuff is prob wrong cause one of the odo pods was dead
    public static OctoQuadConfig localizerConfig = new OctoQuadConfig(c -> {
        c.name.set("octoquad");
        c.xPodPort.set(1);
        c.yPodPort.set(0);
        c.ticksPerUnit.set(487.875);
        c.xPodOffset.set(2.598); // from cad but i trust it more
        c.yPodOffset.set(2.264); // from cad but i trust it more
        c.xPodDirection.set(OctoQuad.EncoderDirection.FORWARD);
        c.yPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
        c.i2cRecoveryMode.set(OctoQuad.I2cRecoveryMode.MODE_1_PERIPH_RST_ON_FRAME_ERR);
        c.headingScalar.set(1.01828);
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.28395704217991863);
        Controller secondaryTranslationalForward = Controller.proportional(0.1049145396135724);
        Controller primaryTranslationalLateral = Controller.proportional(0.3428231469950291);
        Controller secondaryTranslationalLateral = Controller.proportional(0.12666399241146598);

        c.forwardTranslational.set(
            Controller.piecewise(secondaryTranslationalForward).put(
                2.5,
                primaryTranslationalForward
            )
        );
        c.strafeTranslational.set(
            Controller.piecewise(secondaryTranslationalLateral).put(
                2.5,
                primaryTranslationalLateral
            )
        );

        c.coast.set(Controller.proportionalFeedforward(0.01079904153957472));
        c.brake.set(Controller.proportionalFeedforward(0.009179185308638512));

        c.headingFeedback.set(Controller.proportional(4.147776450022238));
        c.headingBrakeCoefficients.set(
            Vector2D.cartesian(0.04379310349541531, 0.004956338721491027)
        );

        c.linearBrakeCoefficients.set(Matrix.diag(0.10012668456353997, 0.050384955639263866));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.001271398610072872, 0.0018072979525181625));

        c.maxAchievableForwardVelocity.set(90.17261302872845);
        c.maxAchievableStrafeVelocity.set(72.09872492436867);
        c.naturalForwardDeceleration.set(33.961982607863476);
        c.naturalStrafeDeceleration.set(64.77010240081039);
    });

    /*  public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.28921569321161206);
        Controller secondaryTranslationalForward = Controller.proportional(0.10685747065603969);
        Controller primaryTranslationalLateral = Controller.proportional(0.419135079202836);
        Controller secondaryTranslationalLateral = Controller.proportional(0.1548592122698675);

        c.forwardTranslational.set(
            Controller.piecewise(secondaryTranslationalForward).put(
                2.5,
                primaryTranslationalForward
            )
        );
        c.strafeTranslational.set(
            Controller.piecewise(secondaryTranslationalLateral).put(
                2.5,
                primaryTranslationalLateral
            )
        );

        c.coast.set(Controller.proportionalFeedforward(0.010792164887014842));
        c.brake.set(Controller.proportionalFeedforward(0.009173340153962616));

        c.headingFeedback.set(Controller.proportional(1.8483533707845325));
        c.headingBrakeCoefficients.set(
            Vector2D.cartesian(0.042727007558152924, 0.0027955105757081437)
        );

        c.linearBrakeCoefficients.set(Matrix.diag(0.0764604129327956, 0.04808161569969401));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0016059410924037809, 0.0018977023632466199));

        c.maxAchievableForwardVelocity.set(89.2980201790953);
        c.maxAchievableStrafeVelocity.set(73.09254179785334);
        c.naturalForwardDeceleration.set(35.97919628362501);
        c.naturalStrafeDeceleration.set(65.42587076498502);
    });*/
}
