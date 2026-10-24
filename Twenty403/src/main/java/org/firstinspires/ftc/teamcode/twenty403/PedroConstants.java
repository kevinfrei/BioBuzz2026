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
        c.frontLeftName.set("fl");
        c.frontRightName.set("fr");
        c.backLeftName.set("rl");
        c.backRightName.set("rr");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });
    // Ran on Oct 23 2026
    public static TwoWheelConfig localizerConfig = new TwoWheelConfig(c -> {
        c.xPodName.set("fbodo");
        c.yPodName.set("strafeodo");
        c.imuName.set("adafruit-imu");
        c.xPodOffset.set(-3.95);
        c.yPodOffset.set(-5.67);
        c.forwardTicksToInches.set(0.002014);
        c.strafeTicksToInches.set(0.002014);
        c.xPodDirection.set(Encoder.REVERSE);
        c.yPodDirection.set(Encoder.REVERSE);
        c.imu.set(
            new RevHubIMU(
                new RevHubOrientationOnRobot(
                    RevHubOrientationOnRobot.LogoFacingDirection.UP,
                    RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
                )
            )
        );
    });

    public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
        Controller primaryTranslationalForward = Controller.proportional(0.2833077272955512);
        Controller secondaryTranslationalForward = Controller.proportional(0.1046746351138119);
        Controller primaryTranslationalLateral = Controller.proportional(0.42485505706781734);
        Controller secondaryTranslationalLateral = Controller.proportional(0.1569725912503547);

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

        c.coast.set(Controller.proportionalFeedforward(0.011880823934200879));
        c.brake.set(Controller.proportionalFeedforward(0.010098700344070746));

        // Lower values jiggle *less8
        c.headingFeedback.set(Controller.proportional(3.263295808015299));
        c.headingBrakeCoefficients.set(
            Vector2D.cartesian(0.06854604503841423, -0.0019709745617720845)
        );

        c.linearBrakeCoefficients.set(Matrix.diag(0.08929294098815413, 0.05156861139370113));
        c.quadraticBrakeCoefficients.set(Matrix.diag(0.0019785707157205295, 0.002581734940604227));

        c.maxAchievableForwardVelocity.set(80.2824047715213);
        c.maxAchievableStrafeVelocity.set(54.2807052003325);
        c.naturalForwardDeceleration.set(26.27966579345643);
        c.naturalStrafeDeceleration.set(50.71530641366674);
    });
}
