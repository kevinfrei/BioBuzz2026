package org.firstinspires.ftc.teamcode.twenty403;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.CustomIMU;
import com.pedropathing.revhub.localizers.Encoder;
import com.pedropathing.revhub.localizers.OctoQuadConfig;
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.pedropathing.revhub.localizers.RevHubIMU;
import com.pedropathing.revhub.localizers.TwoWheelConfig;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.sensor.AdafruitIMU;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.twenty403.helpers.AdafruitCustomIMU;

public class PedroConstants {

    // This should set before the follower is created
    public static AdafruitIMU ext_imu = null;

    public static Follower create(HardwareMap h) {
        return new Follower(
            new OctoQuadLocalizer(h, localizerConfig),
            new Mecanum(h, drivetrainConfig),
            new Foresight(foresightConfig)
        );
    }

    // TODO: Run the tuners on the bot, fill these it with the results:
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(Setup.HardwareNames.FLMOTOR);
        c.frontRightName.set(Setup.HardwareNames.FRMOTOR);
        c.backLeftName.set(Setup.HardwareNames.RLMOTOR);
        c.backRightName.set(Setup.HardwareNames.RRMOTOR);
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });
    // Ran on Oct 23 2026
    public static OctoQuadConfig localizerConfig = new OctoQuadConfig(c -> {
        c.name.set("octoquad");
        c.xPodPort.set(3);
        c.yPodPort.set(2);
        c.ticksPerUnit.set(505.316944406);
        c.xPodOffset.set(-0.05905511811023623);
        c.yPodOffset.set(-6.141732283464568);
        c.xPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
        c.yPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
        c.i2cRecoveryMode.set(OctoQuad.I2cRecoveryMode.MODE_1_PERIPH_RST_ON_FRAME_ERR);
        c.headingScalar.set(1.0172521379226043);
    });
    public static TwoWheelConfig oldLocalizerConfig = new TwoWheelConfig(c -> {
        c.xPodName.set(Setup.HardwareNames.FB_ODO);
        c.yPodName.set(Setup.HardwareNames.STRAFE_ODO);
        c.imuName.set(
            Setup.Connected.EXTERNALIMU ? Setup.HardwareNames.EXTERNALIMU : Setup.HardwareNames.IMU
        );
        if (Setup.Connected.EXTERNALIMU) {
            AdafruitCustomIMU imu = new AdafruitCustomIMU(ext_imu);
            c.imu.set(imu);
        }
        c.xPodOffset.set(-0.413);
        c.yPodOffset.set(-5.67);
        c.forwardTicksToInches.set(0.0020135);
        c.strafeTicksToInches.set(0.0020135);
        c.xPodDirection.set(Encoder.REVERSE);
        c.yPodDirection.set(Encoder.REVERSE);
        c.imu.set(
            new RevHubIMU(
                new RevHubOrientationOnRobot(
                    Setup.Connected.EXTERNALIMU
                        ? RevHubOrientationOnRobot.LogoFacingDirection.UP
                        : RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
                    Setup.Connected.EXTERNALIMU
                        ? RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
                        : RevHubOrientationOnRobot.UsbFacingDirection.UP
                )
            )
        );
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.2785348999832107);
        Controller secondaryTranslationalForward = Controller.proportional(0.10291120295419666);
        Controller primaryTranslationalLateral = Controller.proportional(0.4275314354262314);
        Controller secondaryTranslationalLateral = Controller.proportional(0.15796144153964187);

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

        c.coast.set(Controller.proportionalFeedforward(0.012069761957410004));
        c.brake.set(Controller.proportionalFeedforward(0.010259297663798503));

        c.headingFeedback.set(Controller.proportional(3.2073599278729015));
        c.headingBrakeCoefficients.set(
            Vector2D.cartesian(0.059299684851447654, 0.0069180845214850085)
        );

        c.linearBrakeCoefficients.set(Matrix.diag(0.08611023457568019, 0.04405586069455108));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0022437236542938617, 0.0025909933879756804));

        c.maxAchievableForwardVelocity.set(81.30848431898026);
        c.maxAchievableStrafeVelocity.set(58.23300640932813);
        c.naturalForwardDeceleration.set(25.36341634595008);
        c.naturalStrafeDeceleration.set(47.44720604384475);
    });

    public static class Poses {

        public static Pose South = new Pose(7, 80, 0);
        public static Pose North = new Pose(7, 135, 0);
    }
}
