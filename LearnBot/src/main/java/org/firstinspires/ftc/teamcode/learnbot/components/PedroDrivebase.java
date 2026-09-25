package org.firstinspires.ftc.teamcode.learnbot.components;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.controllers.Controller;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Matrix;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.pedropathing.paths.Path;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.OctoQuadConfig;
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.technototes.library.command.Command;
import com.technototes.library.control.Stick;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.subsystem.TargetAcquisition;
import com.technototes.library.util.Alliance;
import com.technototes.library.util.MathUtils;
import com.technototes.library.util.PIDFController;
import java.util.Locale;
import java.util.function.DoubleSupplier;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.learnbot.Setup;

public class PedroDrivebase {

    @Configurable
    public static class Config {

        // Names of motors:
        public static String FLMOTOR = "fl";
        public static String FRMOTOR = "fr";
        public static String RLMOTOR = "rl";
        public static String RRMOTOR = "rr";

        // Max power scaling for translational driving:
        public static double SNAIL_SPEED = 0.40;
        public static double NORMAL_SPEED = 0.8;
        public static double TURBO_SPEED = 1.0;
        public static double AUTO_SPEED = 0.95;

        // The 'fastest' the robot can turn (0: not turning, 1.0: Fastest possible)
        public static double SNAIL_TURN = 0.25;
        public static double NORMAL_TURN = 0.5;
        public static double TURBO_TURN = 1.0;

        public static double STICK_DEAD_ZONE = 0.05;

        /**** Stuff for the PedroPathing follower ****/

        public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
            c.frontLeftName.set(FLMOTOR);
            c.frontRightName.set(FRMOTOR);
            c.backLeftName.set(RLMOTOR);
            c.backRightName.set(RRMOTOR);
            c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
            c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
            c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
            c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
        });

        public static OctoQuadConfig localizerConfig = new OctoQuadConfig(c -> {
            c.name.set(Setup.HardwareNames.OCTOQUAD_MK2);
            c.xPodPort.set(7);
            c.yPodPort.set(6);
            c.ticksPerUnit.set(505.316944406);
            c.xPodOffset.set(-2.34251968503937);
            c.yPodOffset.set(0.688976377952756);
            c.xPodDirection.set(OctoQuad.EncoderDirection.FORWARD);
            c.yPodDirection.set(OctoQuad.EncoderDirection.REVERSE);
            c.globalDistanceUnit.set(DistanceUnit.INCH);
            c.offsetUnits.set(DistanceUnit.INCH);
            c.i2cRecoveryMode.set(OctoQuad.I2cRecoveryMode.MODE_1_PERIPH_RST_ON_FRAME_ERR);
            c.headingScalar.set(1.0144895113606114);
        });

        public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
            Controller primaryTranslationalForward = Controller.proportional(0.17242498230829773);
            Controller secondaryTranslationalForward = Controller.proportional(0.06370642368253522);
            Controller primaryTranslationalLateral = Controller.proportional(0.22409480643857885);
            Controller secondaryTranslationalLateral = Controller.proportional(0.08279704305554568);

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

            c.coast.set(Controller.proportionalFeedforward(0.017881248203488014));
            c.brake.set(Controller.proportionalFeedforward(0.015199060972964811));

            c.headingFeedback.set(Controller.proportional(2.415440556391569));
            c.headingBrakeCoefficients.set(
                Vector2D.cartesian(0.04427148791685309, 0.0024816585608359036)
            );

            c.linearBrakeCoefficients.set(Matrix.diag(0.061406279185640913, 0.04023543050788815));
            c.quadraticBrakeCoefficients.set(
                Matrix.diag(9.568312745336765E-4, 0.0012968587256159201)
            );

            c.maxAchievableForwardVelocity.set(57.977352408951944);
            c.maxAchievableStrafeVelocity.set(48.71239910399553);
            c.naturalForwardDeceleration.set(31.96456257414348);
            c.naturalStrafeDeceleration.set(53.50127263937453);
        });

        public static Follower create(HardwareMap h) {
            return new Follower(
                new OctoQuadLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig)
            );
        }
    }

    public static class Commands {

        protected static Component self;

        public static Command ManualDriveForward(double fwd) {
            return new DrivePowerImpl(fwd, 0, 0);
        }

        public static Command ManualDriveStrafe(double strafe) {
            return new DrivePowerImpl(0, strafe, 0);
        }

        public static Command ManualDriveTurn(double turn) {
            return new DrivePowerImpl(0, 0, turn);
        }

        public static Command JoystickDrive(Stick xyStick, Stick rotStick) {
            return new JoystickImpl(
                xyStick.getYSupplier(),
                xyStick.getXSupplier(),
                rotStick.getXSupplier()
            );
        }

        public static Command JoystickDrive(
            DoubleSupplier fwdSup,
            DoubleSupplier strafeSup,
            DoubleSupplier rotSup
        ) {
            return new JoystickImpl(fwdSup, strafeSup, rotSup);
        }

        public static Command FollowPath(Path p) {
            return new FollowPathImpl(p);
        }

        public static Command FollowPath(Pose startPose, Path p) {
            return new FollowPathImpl(startPose, p);
        }

        public static Command FollowPath(Path p, boolean readCurPose) {
            return new FollowPathImpl(p, readCurPose);
        }

        public static Command TurboSpeed() {
            return self::SetTurboSpeed;
        }

        public static Command SnailSpeed() {
            return self::SetSnailSpeed;
        }

        public static Command NormalSpeed() {
            return self::SetNormalSpeed;
        }

        public static Command VisionDriving() {
            return self::SetVisionDriving;
        }

        public static Command ResumeDriving() {
            return self::ResumeDriving;
        }

        public static Command HoldRotation() {
            return self::SetHoldRotation;
        }

        public static Command TangentRotation() {
            return self::SetTangentRotation;
        }

        public static Command SetBidirectionalRotation() {
            return self::SetBidirectionalRotation;
        }

        public static Command SetVisionRotation() {
            return self::SetVisionRotation;
        }

        // Helper for DrivePower-based manual driving commands
        protected static class DrivePowerImpl implements Command {

            DrivePowers p;

            public DrivePowerImpl(double fwd, double strafe, double turn) {
                p = new DrivePowers(fwd, strafe, turn);
            }

            @Override
            public void execute() {
                Follower f = getFollower();
                // If we don't have a drive base connected, we don't get a follower
                if (f != null) {
                    f.drivetrain.drive(p, true);
                }
            }
        }

        // Helper for any Joystick driving commands
        protected static class JoystickImpl implements Command {

            // The sticks for driving (probably CommandAxis suppliers)
            // Note that the stick values returned are oriented like this:
            // Up is a negative value, down is a positive value.
            // Left is a negative value, right is a positive value.
            DoubleSupplier x, y, r;

            public JoystickImpl(
                DoubleSupplier fwdSup,
                DoubleSupplier strafeSup,
                DoubleSupplier rotSup
            ) {
                x = strafeSup;
                y = fwdSup;
                r = rotSup;
                addRequirements(self);
            }

            @Override
            public void initialize() {
                self.StartTele();
            }

            @Override
            public void execute() {
                // Read the stick values, and pass them to the drive base.
                // We invert the signs because both up and left are negative, which is opposite Pedro.
                // The drivebase can do all the filtering & drive mode shenanigans it wants. We're just
                // here to read the joysticks and send the values to the drivebase...
                double fwdVal = -MathUtils.deadZoneScale(y.getAsDouble(), Config.STICK_DEAD_ZONE);
                double strafeVal = -MathUtils.deadZoneScale(
                    x.getAsDouble(),
                    Config.STICK_DEAD_ZONE
                );
                double rotVal = -MathUtils.deadZoneScale(r.getAsDouble(), Config.STICK_DEAD_ZONE);
                self.RegisterJoystickRead(fwdVal, strafeVal, rotVal);
            }

            @Override
            public boolean isFinished() {
                return false;
            }
        }

        // *Untested* command to have Pedro V3 follow a path
        protected static class FollowPathImpl implements Command {

            public Path path;
            public Pose begin;
            public boolean currentPose;

            public FollowPathImpl(Path p) {
                path = p;
            }

            public FollowPathImpl(Pose startPose, Path p) {
                path = p;
                currentPose = true;
                begin = startPose;
            }

            public FollowPathImpl(Path p, boolean currPose) {
                path = p;
                currentPose = currPose;
                begin = null;
            }

            @Override
            public void initialize() {
                Follower f = getFollower();
                if (f == null) {
                    // No drivebase connected...
                    return;
                }
                if (currentPose) {
                    f.setPose(begin == null ? f.pose() : begin);
                }
                f.follow(path);
            }

            @Override
            public boolean isFinished() {
                Follower f = getFollower();
                return f == null || !f.isBusy();
            }

            @Override
            public void execute() {
                Follower f = getFollower();
                if (f != null) {
                    f.update();
                }
            }
        }
    }

    // The PedroPath follower, to let us actually make the bot move:
    protected static Follower follower = null;

    public static Follower createFollower(HardwareMap hardwareMap) {
        if (follower != null) {
            follower = Config.create(hardwareMap);
        }
        return follower;
    }

    public static Follower getFollower() {
        // TODO: Can make this an on-demand creation if we'd like :)
        return follower;
    }

    /* Recall, the Pedro Path coordinate system:
                 [Refs/score table]
+-------------------------------------------------+
|(0,144)             ^ +y                (144,144)|
|                                                 |
|                    90 deg                       |
|                                                 |
|                                                 |
| <== -x 180 deg     (72,72)        0 deg +x ==>  |
|                                                 |
|                                                 |
| Red Drive Team                  Blue Drive Team |
|                    270 deg                      |
|(0,0)                v -y                 (144,0)|
+-------------------------------------------------+
                   [Audience]
 */

    @Configurable
    public static class Component implements Subsystem, Loggable {

        private static final double HalfPi = Math.PI * 0.5;
        private static final double TwoPi = Math.PI * 2;

        // Encapsulated to allow easy "previous driving style" tracking
        private static class DrivingStyle {

            public DrivingStyle() {
                perspective = DrivingPerspective.FieldCentric;
                rotation = RotationMode.Free;
                translation = TranslationMode.Free;
                rotationSpeed = Config.NORMAL_TURN;
                translationSpeed = Config.NORMAL_SPEED;
            }

            public DrivingStyle(DrivingStyle other) {
                perspective = other.perspective;
                rotation = other.rotation;
                translation = other.translation;
                rotationSpeed = other.rotationSpeed;
                translationSpeed = other.translationSpeed;
            }

            public DrivingPerspective perspective;
            public RotationMode rotation;
            public TranslationMode translation;
            public double rotationSpeed;
            public double translationSpeed;
        }

        // This concerns how the bot is rotating (or not)
        public enum RotationMode {
            Free,
            Snap,
            Hold, // Hold the current heading
            Tangential, // Aim toward the translational direction
            Bidirectional, // Like tangential, but will turn to the closest tangential direction
            Vision, // Use Vision to find the target & aim toward it
            Target_NYI, // The controller is used to specify a desired heading
            None,
        }

        // This concerns how the bot is moving around the field
        public enum TranslationMode {
            Free,
            Square,
            Hold,
            Vision, // Drive toward the target using vision
            Target_NYI, // Drive toward an external target (that is movable using the controller)
            None,
        }

        public enum DrivingPerspective {
            RobotCentric,
            FieldCentric,
        }

        // The vision subsystem, for vision-based driving stuff
        public TargetAcquisition targetAcquisition;

        // The direction of the 3 axes for manual control: Should be updated by a joystick command
        public double strafe, forward, rotation;

        // These numbers seem a little weird, but the target value is *radians* and the output is
        // 'stick distance'.
        public static PIDFCoefficients turningPIDValues = new PIDFCoefficients(0.9, 0.3, 0.05, 0);
        // This is the limit above which we don't consider "error" for the turning PID controller.
        // 4.5 degree 'range' for the error to start accumulating
        public static double WIND_UP_LIMIT = HalfPi * 0.05;

        // These are here to let me change the PIDF on they fly, rather than forcing me to constantly
        // restart the opmode.
        private double P = 0;
        private double I = 0;
        private double D = 0;
        private double F = 0;

        private PIDFController turningPIDController = new PIDFController(turningPIDValues);
        private boolean turnPidStarted = false;

        public double RotationTransform(double r) {
            // This gives you more sensitivity at the low end:
            // .25^2 = .0625, .5^2 = .25, .75^2 = .5625, etc...
            return Math.copySign(r * r * driveStyle.rotationSpeed, r);
        }

        // The offset heading for field-relative controls
        double headingOffsetRadians;
        double targetHeading;
        double directedHeading; // used for Target-based rotation mode
        ElapsedTime lastReading; // also used for Target-based rotation mode
        double curHeading;

        // used to keep the directions straight
        Alliance alliance;

        // Used to keep track of the previous drive style when using the "StayPut" operation
        private DrivingStyle prevDriveStyle;

        public double[] snapRadians;

        DrivingStyle driveStyle;

        Pose holdPose;
        boolean started;

        public RotationMode GetRotationMode() {
            return driveStyle.rotation;
        }

        public void SetRotationalMode(RotationMode r) {
            driveStyle.rotation = r;
        }

        public TranslationMode GetTranslationMode() {
            return driveStyle.translation;
        }

        public void SetTranslationMode(TranslationMode t) {
            driveStyle.translation = t;
        }

        public DrivingPerspective GetPerspective() {
            return driveStyle.perspective;
        }

        private double BaseHeadingOffset() {
            return alliance == Alliance.BLUE ? Math.PI : 0;
        }

        private double GetHeadingOffsetRadians() {
            return driveStyle.perspective == DrivingPerspective.FieldCentric
                ? headingOffsetRadians
                : 0;
        }

        public Component(Follower f, TargetAcquisition viz, Alliance all) {
            Commands.self = this;
            started = false;
            follower = f;
            targetAcquisition = viz;
            alliance = all;
            headingOffsetRadians = BaseHeadingOffset();
            holdPose = null;
            forward = 0;
            strafe = 0;
            rotation = 0;
            targetHeading = 0;
            driveStyle = new DrivingStyle();
            directedHeading = Double.NaN;
            lastReading = new ElapsedTime();
            // Snap to these angles
            // You need the final point there for "wrap-around" to work properly
            snapRadians = new double[] { -Math.PI, -HalfPi, 0, HalfPi, Math.PI };
            SetFieldCentricMode();
            SetFreeDriving();
            SetNormalSpeed();
        }

        public Component(Follower f, Alliance all) {
            this(f, null, all);
        }

        public Component(Follower f, TargetAcquisition viz) {
            this(f, viz, Alliance.NONE);
        }

        public Component(Follower f) {
            this(f, null, Alliance.NONE);
        }

        // Do we need a function to start autonomous driving?
        // Command to start teleop driving
        public void StartTele() {
            started = true;
            follower.manual();
        }

        // Methods to bind to buttons (Commands)
        public void ResetGyro() {
            headingOffsetRadians = MathUtils.normalizeRadians(
                follower.pose().heading() + BaseHeadingOffset()
            );
        }

        public void SetSnailSpeed() {
            // TODO: follower.setMaxPowerScaling(Config.SNAIL_SPEED);
            driveStyle.translationSpeed = Config.SNAIL_SPEED;
            driveStyle.rotationSpeed = Config.SNAIL_TURN;
        }

        public void SetNormalSpeed() {
            // TODO: follower.setMaxPowerScaling(Config.NORMAL_SPEED);
            driveStyle.translationSpeed = Config.NORMAL_SPEED;
            driveStyle.rotationSpeed = Config.NORMAL_TURN;
        }

        public void SetTurboSpeed() {
            // TODO: follower.setMaxPowerScaling(Config.TURBO_SPEED);
            driveStyle.translationSpeed = Config.TURBO_SPEED;
            driveStyle.rotationSpeed = Config.TURBO_TURN;
        }

        private void SwitchDrivingMode(TranslationMode x, RotationMode r) {
            TranslationMode px = driveStyle.translation;
            RotationMode pr = driveStyle.rotation;
            if (px == x && pr == r) {
                return;
            }
            driveStyle.translation = x;
            driveStyle.rotation = r;
            if (px == TranslationMode.Hold && x != TranslationMode.Hold) {
                // If we're going to stop holding a position, start Tele back up
                StartTele();
            } else if (px != TranslationMode.Hold && x == TranslationMode.Hold) {
                // If we've switched *to* holding a pose, start the follower
                holdPose = follower.pose();
                follower.hold(holdPose, false);
            } else if (pr != RotationMode.Hold && driveStyle.rotation == RotationMode.Hold) {
                // If we're transitioning to a Rotational hold, just set the pos in the holdPose
                holdPose = follower.pose();
            }
            if (r == RotationMode.Target_NYI) {
                directedHeading = Double.NaN;
            }
            turnPidStarted = false;
        }

        public void SetFreeDriving() {
            SwitchDrivingMode(TranslationMode.Free, RotationMode.Free);
        }

        public void SetFreeRotation() {
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Free);
        }

        // The list of rotation points (in degrees) you want to snap to
        public void SetSnapRotation(double... degrees) {
            snapRadians = new double[degrees.length + 1];
            double lowest = TwoPi;
            for (int i = 0; i < degrees.length; i++) {
                snapRadians[i] = MathUtils.normalizeDeltaRadians(Math.toRadians(degrees[i]));
                if (snapRadians[i] < lowest) {
                    lowest = snapRadians[i];
                }
            }
            // For wrap-around to work properly, we'll add the first location, but past the wrap-around
            // point of the circle. So, if you want to snap to 15 and -170, we'll append 190 so that 175
            // will wind up going to 190 (which then normalizes back to -170). Neat, huh?
            snapRadians[degrees.length] = lowest + TwoPi;
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Snap);
        }

        public void SetSnapRotation() {
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Snap);
        }

        public void SetTangentRotation() {
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Tangential);
        }

        public void SetBidirectionalRotation() {
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Bidirectional);
        }

        public void SetVisionRotation() {
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Vision);
        }

        public void SetHoldRotation() {
            SwitchDrivingMode(GetTranslationMode(), RotationMode.Hold);
        }

        public void HoldCurrentPosition() {
            SwitchDrivingMode(TranslationMode.Hold, RotationMode.Hold);
        }

        public void SetFreeMotion() {
            SwitchDrivingMode(TranslationMode.Free, GetRotationMode());
        }

        public void SetSquareMotion() {
            SwitchDrivingMode(TranslationMode.Square, GetRotationMode());
        }

        public void SetVisionDriving() {
            prevDriveStyle = new DrivingStyle(driveStyle);
            SwitchDrivingMode(TranslationMode.Vision, RotationMode.Vision);
        }

        public void SetRobotCentricMode() {
            driveStyle.perspective = DrivingPerspective.RobotCentric;
        }

        public void SetFieldCentricMode() {
            driveStyle.perspective = DrivingPerspective.FieldCentric;
        }

        // This, when well tuned, should allow the driver to ignore minor bot-bumps and the like.
        public void SetTargetBasedRotation() {
            driveStyle.rotation = RotationMode.Target_NYI;
        }

        // This, when well tuned, should allow the programmers to ignore frictional differences between
        // wheels on the drivebase...
        public void SetTargetBasedMotion() {
            driveStyle.translation = TranslationMode.Target_NYI;
        }

        // Some just slightly more complex commands:
        public void StayPut() {
            if (prevDriveStyle == null) {
                prevDriveStyle = driveStyle;
            }
            driveStyle = new DrivingStyle(prevDriveStyle);
            HoldCurrentPosition();
            SetTurboSpeed();
        }

        public void ResumeDriving() {
            if (prevDriveStyle == null) {
                SetFreeDriving();
                SetNormalSpeed();
            } else {
                driveStyle = prevDriveStyle;
                prevDriveStyle = null;
                StartTele();
            }
        }

        public void RegisterJoystickRead(double f, double s, double r) {
            this.forward = f;
            this.strafe = s;
            this.rotation = r;
        }

        @Override
        public void periodic() {
            // I think this is unnecessary:
            if (
                turningPIDValues.p != P ||
                turningPIDValues.i != I ||
                turningPIDValues.d != D ||
                turningPIDValues.f != F
            ) {
                // Someone changed the PIDF on the panels: Updated it in realtime...
                turningPIDController = new PIDFController(turningPIDValues);
                P = turningPIDValues.p;
                I = turningPIDValues.i;
                D = turningPIDValues.d;
                F = turningPIDValues.f;
            }
            // If subsystem is busy it is running a path, just ignore the stick.
            ShowDriveInfo();
            if (follower == null) {
                return;
            }
            if (follower.isBusy()) {
                follower.update();
                drvVec = "busy";
                return;
            }
            if (!started) {
                return;
            }

            if (driveStyle.translation == TranslationMode.Square) {
                // for square driving, we only use one of the two translation directions:
                if (Math.abs(forward) > Math.abs(strafe)) {
                    strafe = 0;
                } else {
                    forward = 0;
                }
            }
            double rot = getRotation();
            boolean botCentric = driveStyle.perspective == DrivingPerspective.RobotCentric;
            if (driveStyle.translation == TranslationMode.Vision) {
                // For vision *translation* we will move forward or backward to maintain a 'size' of the
                // target. The 'a' part of the result is the percentage of the total image that the
                // target is filling, so the closer you are, the larger the area of the image. It's
                // kinda dopey, but works just fine...
                double distance = targetAcquisition.getDistance();
                if (distance >= 0) {
                    forward = distance;
                    botCentric = true;
                }
            }
            ShowDriveVectors(forward, strafe, rot, GetHeadingOffsetRadians());
            DrivePowers p = botCentric
                ? new DrivePowers(forward, strafe, rot)
                : ManualDrive.fieldCentric(forward, strafe, rot, curHeading);
            follower.manual(p);
            follower.update();
        }

        double getRotation() {
            // Negative, because pushing left is negative, but that is a positive change in Pedro's
            // coordinate system.
            curHeading = MathUtils.normalizeDeltaRadians(
                follower.pose().heading() - GetHeadingOffsetRadians()
            );
            switch (driveStyle.rotation) {
                case Target_NYI:
                // The controller is used to specify a desired heading: NYI
                // To implement this, we need to know a "degrees/sec" change for each speed.
                // From basic observations: slow: 90 deg/sec; normal: 180 deg/sec, fast: 300 deg/sec
                // Once we have that, we need to have a timer between updates, as well as a decent
                // PIDF for the heading, so that we can get to the desired location quickly
                // and accurately. If the rotation stick goes to zero, we probably just hold the
                // current location.
                case Free:
                    return RotationTransform(rotation);
                case Snap:
                    // Angle-focused driving styles override target-based driving mode
                    targetHeading = MathUtils.closestTo(curHeading, snapRadians);
                    break;
                case Hold:
                    // Hold the current heading
                    if (driveStyle.translation != TranslationMode.Hold && holdPose != null) {
                        targetHeading = MathUtils.normalizeDeltaRadians(
                            holdPose.heading() - GetHeadingOffsetRadians()
                        );
                    } else {
                        // This is weird: We have a rotational hold, but not a translational hold, and
                        // no holdPose. This is probably an error
                        targetHeading = 0;
                    }
                    break;
                case Tangential: // Aim toward the translational direction
                    // Tangential is an angle-focused driving style, but the heading
                    // is strictly in the direction of the stick. Logically, this is an attempt to
                    // eliminate "actual" strafing: The robot should be oriented to drive forward in the
                    // direction of the stick
                    if (Math.abs(strafe) > 0 || Math.abs(forward) > 0) {
                        targetHeading = MathUtils.normalizeDeltaRadians(
                            Math.atan2(strafe, forward)
                        );
                    } else {
                        return 0;
                    }
                    break;
                case Bidirectional: // Align the bot to the translational direction
                    // Bidirectional is an angle-focused driving style, but the heading is strictly in
                    // the direction of the stick, but with a twist: If you flip the direction you're
                    // driving, the bot will drive in reverse, rather than fully rotate.
                    if (Math.abs(strafe) > 0 || Math.abs(forward) > 0) {
                        targetHeading = MathUtils.normalizeDeltaRadians(
                            Math.atan2(strafe, forward)
                        );
                        // targetHeading should be within 90 degrees of curHeading, if it's not, flip it
                        if (
                            Math.abs(MathUtils.normalizeDeltaRadians(targetHeading - curHeading)) >
                            HalfPi
                        ) {
                            targetHeading = MathUtils.normalizeDeltaRadians(
                                targetHeading + Math.PI
                            );
                        }
                    } else {
                        return 0;
                    }
                    break;
                case Vision: // Use Vision to find the target & aim toward it
                    double x =
                        targetAcquisition == null
                            ? Double.NaN
                            : targetAcquisition.getHorizontalPosition();
                    if (!Double.isNaN(x)) {
                        targetHeading = MathUtils.normalizeDeltaRadians(
                            curHeading + Math.toRadians(x)
                        );
                    } else {
                        return RotationTransform(rotation);
                    }
                    break;
                default:
                    return 0;
            }
            // Use the turn PID controller to get to the target heading
            if (!turnPidStarted) {
                turningPIDController.reset();
                turnPidStarted = true;
            }
            if (
                Math.abs(MathUtils.normalizeDeltaRadians(targetHeading - curHeading)) >
                WIND_UP_LIMIT
            ) {
                // The goal is to prevent *massive* early error from making the I value useless.
                turningPIDController.reset();
            }
            turningPIDController.setTarget(targetHeading);
            // We need to handle a weird case: When the current/target heading values are close
            // to the angle wrap-around point, we should switch one of them from -180 to 180 to 0-360
            // or -360 - 0 instead. This minimizes the error. Without this, when your target heading is
            // near that wrap-around location, you'll get some serious bot jiggling, without an obvious
            // reason.
            if (Math.abs(curHeading - targetHeading) > Math.PI) {
                curHeading += Math.signum(targetHeading) * TwoPi;
            }
            return turningPIDController.update(curHeading);
        }

        // Everything below here is just for displaying/diagnostics

        @Log(name = "DrvMode")
        public static String drvMode = "";

        @Log(name = "DrvVec")
        public static String drvVec = "";

        @Log(name = "Pose")
        public static String drvLoc = "";

        private void ShowDriveInfo() {
            switch (driveStyle.rotation) {
                case Target_NYI:
                    drvMode = "rTgt";
                    break;
                case Free:
                    drvMode = "rFre";
                    break;
                case Snap:
                    drvMode = "rSnp";
                    break;
                case Hold:
                    drvMode = "rHld";
                    break;
                case Tangential:
                    drvMode = "rTan";
                    break;
                case Bidirectional:
                    drvMode = "rBid";
                    break;
                case Vision:
                    drvMode = "rViz";
                    break;
                default:
                    drvMode = "rUnk";
                    break;
            }
            switch (driveStyle.translation) {
                case Free:
                    drvMode += "xFre";
                    break;
                case Square:
                    drvMode += "xSqu";
                    break;
                case Hold:
                    drvMode += "xHld";
                    break;
                case Vision:
                    drvMode += "xViz";
                    break;
                default:
                    drvMode += "xUnk";
                    break;
            }
            switch (driveStyle.perspective) {
                case RobotCentric:
                    drvMode += ":Bot";
                    break;
                case FieldCentric:
                    drvMode += ":Fld";
                    break;
                default:
                    drvMode += ":[?]";
                    break;
            }
            drvMode += String.format(Locale.ENGLISH, " Max:%.2f", driveStyle.translationSpeed);
            Pose curPose = follower.pose();
            drvLoc = String.format(
                Locale.ENGLISH,
                "X:%.2f Y:%.2f H:%.1f° T:%.1f°",
                curPose.x(),
                curPose.y(),
                Math.toDegrees(curPose.heading()),
                Math.toDegrees(targetHeading)
            );
        }

        private void ShowDriveVectors(double fwd, double strafe, double rot, double offset) {
            drvVec = String.format(
                Locale.ENGLISH,
                "f %.2f s %.2f r %.2f@%.1f° [%.1f°]",
                fwd,
                strafe,
                rot,
                Math.toDegrees(curHeading),
                Math.toDegrees(MathUtils.normalizeDeltaRadians(offset))
            );
        }
    }
}
