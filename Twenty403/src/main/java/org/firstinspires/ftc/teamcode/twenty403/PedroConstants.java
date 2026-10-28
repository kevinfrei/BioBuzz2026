package org.firstinspires.ftc.teamcode.twenty403;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Vector2D;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.Encoder;
import com.pedropathing.revhub.localizers.RevHubIMU;
import com.pedropathing.revhub.localizers.TwoWheelConfig;
import com.pedropathing.revhub.localizers.TwoWheelLocalizer;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class PedroConstants {

    public static Follower create(HardwareMap h) {
        return new Follower(
            new TwoWheelLocalizer(h, localizerConfig),
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
    public static TwoWheelConfig localizerConfig = new TwoWheelConfig(c -> {
        c.xPodName.set(Setup.HardwareNames.FB_ODO);
        c.yPodName.set(Setup.HardwareNames.STRAFE_ODO);
        c.imuName.set(
            Setup.Connected.EXTERNALIMU ? Setup.HardwareNames.EXTERNALIMU : Setup.HardwareNames.IMU
        );
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
        Controller primaryTranslationalForward = Controller.proportional(3.140914166223175);
        Controller secondaryTranslationalForward = Controller.proportional(1.16048385764724);
        Controller primaryTranslationalLateral = Controller.proportional(0.3869868831953519);
        Controller secondaryTranslationalLateral = Controller.proportional(0.1429813128607202);

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

        c.coast.set(Controller.proportionalFeedforward(0.013173340432575217));
        c.brake.set(Controller.proportionalFeedforward(0.011197339367688934));

        // Lower values jiggle *less*
        c.headingFeedback.set(Controller.proportional(1.5));
        c.headingBrakeCoefficients.set(
            Vector2D.cartesian(0.06921667217831645, 0.003996514850370519)
        );

        c.linearBrakeCoefficients.set(Matrix.diag(0.07879625092892711, 0.0581106795791841));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0024436056663298777, 0.0024037888882513982));

        c.maxAchievableForwardVelocity.set(82.19934633520977);
        c.maxAchievableStrafeVelocity.set(54.62715311683887);
        c.naturalForwardDeceleration.set(28.310017444938694);
        c.naturalStrafeDeceleration.set(105.43819925978954);
    });
}
