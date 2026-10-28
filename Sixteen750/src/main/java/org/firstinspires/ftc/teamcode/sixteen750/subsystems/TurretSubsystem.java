package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import static java.lang.Math.clamp;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.util.MathUtils;
import com.technototes.library.util.PIDFController;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
public class TurretSubsystem implements Loggable, Subsystem {

    final Robot robot;
    Pose HIVE_TARGET1 = new Pose(56.5, 51);
    Pose HIVE_TARGET2 = new Pose(56.5, 93);

    public static boolean turretSOTM = false;
    public static boolean velocitySOTM = false;
    public static boolean hoodSOTM = false;

    public static double TARGET_SWITCH_THRESHOLD = 72;
    public static double TURRET_CENTER = 0.5;
    public static double TURRET_MIN = 0.03;
    public static double TURRET_MAX = 0.97;
    // @Log.Number (name = "target servo Pos")
    public static double targetServoPos = 0;
    public static double FLIGHT_TIME = 0.57;

    @Log.Number(name = "xvelo")
    public static double xVelo = 0;

    @Log.Number(name = "yvelo")
    public static double yVelo = 0;

    @Log.Number(name = "Robot Heading")
    public static double robotHead = 0;

    @Log.Number(name = "Turret Heading")
    public static double turretHead = 0;

    @Log.Number(name = "Target Velocity")
    public static double targetVelo = 0;

    @Log.Number(name = "Current Velocity")
    public static double currentVelo = 0;

    @Log(name = "Target Pose")
    public static Pose targetPose = new Pose(0, 0);

    @Log(name = "sotm targetPose")
    public static Pose sotmTargetPose = new Pose(0, 0);

    @Log.Number(name = "final hood pose")
    public static double hoodAutoPos = 0.5; // final value we feed into the hood for position

    @Log.Number(name = "Manual hood pos")
    public static double manualHoodPos = 0.5; // manual hood pos stuff for tuning

    public static double HOOD_INCREMENT = 0.01; // amount to manually adjust hood by for tuning
    public static double HOOD_POSITION_TO_ANGLE_CONSTANT = 43.42105; // did math to get this it is how the position relates to the angle in degrees
    public static double hoodCompScalar = -0.0324; // the ratio between our error in velocity ~in the couple of hundreds and the change in hood angle 21-38 degrees;
    public static double HOOD_MIN = 0.1;
    public static double HOOD_MAX = 0.5;
    public static double INCREASE = 15; // extremely jank increment and decrement

    @Log.Number(name = "hood calculated Pos")
    public static double hoodTargetPos = 0.5;

    public static double actualTarget = 0; // target for manual velocity tuning
    public static double launcherVelocity = 1500;

    @Log.Number(name = "Auto Velocity")
    public static double autoVelocity = 2000;

    public static double HOOD_REGRESSION_A = 0.00000232206; // slope of hood regression
    public static double HOOD_REGRESSION_B = -0.0003136;
    public static double HOOD_REGRESSION_C = 0.0153873;
    public static double HOOD_REGRESSION_D = -0.315317;
    public static double HOOD_REGRESSION_E = 2.91; // offset to the slope (y intercept)
    public static double LAUNCHER_REGRESSION_A = 0.00657618; // slope of launcher regression
    public static double LAUNCHER_REGRESSION_B = -0.960648;
    public static double LAUNCHER_REGRESSION_C = 51.54356;
    public static double LAUNCHER_REGRESSION_D = -1197.91667; // offset to the launcher regression slope (y-intercept)

    public static double LAUNCHER_REGRESSION_E = 11400.0909;
    public static double LAUNCHER_REGRESSION_E_SOTM = 11375.0909;

    @Log.Number(name = "rawdistance")
    public static double distanceToTarget = 0; // distance to the hive we are aiming at in inches

    @Log.Number(name = "SOTMdistance")
    public static double sotmDistanceToTarget = 0; // distance for sotm

    @Log.Number(name = "err")
    public static double error = 0; // error for launcher velo

    boolean hasHardware;
    Gamepad gamepad;

    EncodedMotor<DcMotorEx> launcher;
    Servo turret1, turret2, hood;
    // This the PIDF controller that's used manage the power.
    // The PIDF values are set in the Config class above.
    private final PIDFController pidfController;
    // The input value is the error of the target velocity that ranges from
    // -2800 to +2800 for a goBilda motor.
    // It's output is a power value in the -1 to +1 range.
    // So, P is probably in the range of .001-ish.
    // For a velocity-targeting PIDF, we probably want an I value, not a D value.
    public static PIDFCoefficients launchPID = new PIDFCoefficients(0.006, 0.000019, 0.0, 0); // 10/3/26 added a p value seems pretty decent
    private Hardware hardware;
    // Stuff used for the Feed Forward function.
    // This one is highly variable, based on the amount of friction in the system

    public static double kStaticFriction = 0.47; // measured 10/12

    public static double kDynamicFriction = 0.463; // measured 10/12

    // This one tends to be somewhere between 0.0035 to 0.005 or so.
    public static double kVelocityConstant = 0.0043;
    public static double MotorResistance = 12 / 9.2;

    public static double GetFrictionConstant(boolean inMotion) {
        return inMotion ? kDynamicFriction : kStaticFriction;
    }

    // GoBilda says stall current of 9.2A at 12V, so V = I * R, R = 12 / 9.2 (about 1.3 ohms)
    // As the motor heats up, resistance also increase, so we could increase this a little bit
    // or maybe increase it over time to counteract that, but this is probably good enough.
    public TurretSubsystem(Hardware h, Robot r) {
        hasHardware = Setup.Connected.TURRETSUBSYSTEM;
        robot = r;
        // Do stuff in here
        if (hasHardware) {
            hardware = h;
            launcher = h.launcher;
            turret1 = h.turret1;
            turret2 = h.turret2;
            hood = h.hood;
            CommandScheduler.register(this);
            gamepad = null;
            launcher.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            launcher = null;
            turret1 = null;
            turret2 = null;
        }
        pidfController = new PIDFController(
            launchPID,
            target ->
                (Math.signum(target) * // signum(<0) = -1, signum(>0) = 1, signum(0) = *0*
                    GetFrictionConstant(getActualVelocity() != 0) +
                    kVelocityConstant * target) /
                hardware.voltage()
        );
    }

    public void Launch() {
        // Spin the motors
        setVelocityTarget(autoVelocity);
    }

    public void ManualLaunch() {
        setVelocityTarget(launcherVelocity);
    }

    // returns the turret angle in radians relative to the robot
    public double getTurretAngle() {
        Pose targetPose = getTargetPose();
        Pose sotmTargetPose = getSotmTargetPose();

        double x, y, robotHead, dx, dy, absoluteAngle, angle; //

        x = robot.follower.pose().x(); // sets x to robots current y position
        y = robot.follower.pose().y(); // sets y to robots current y position
        robotHead = MathUtils.normalizeDeltaAngle(
            robot.follower.pose().heading(),
            AngleUnit.RADIANS
        ); // sets robotHead to robot heading and normalizes to delta radians

        if (turretSOTM) {
            dx = sotmTargetPose.x() - x;
            dy = sotmTargetPose.y() - y;
        } else {
            dx = targetPose.x() - x; // differ    ence between target x and robot x
            dy = targetPose.y() - y; // difference between target y and robot y
        }

        absoluteAngle = Math.atan2(dy, dx); // in radians the target angle of the turret relative to the field

        angle = absoluteAngle - robotHead; // still in radians.......
        return angle;
    }

    //returns the distance from the selected hive target in inches
    public double getDistance() {
        Pose robotPose = robot.follower.pose();
        Pose targetPose = getTargetPose();

        double Distance = robotPose.distance(targetPose);

        return clamp(Distance, 20, 48);
    }

    //returns the distance from the sotm target made the clamp slightly bigger cause why not ig
    public double getSotmDistance() {
        Pose robotPose = robot.follower.pose();
        Pose sotmTargetPose = getSotmTargetPose();

        double sotmDistance = robotPose.distance(sotmTargetPose);

        return clamp(sotmDistance, 16, 52);
    }

    // increment ignore how sus this implementation is btw
    public double increaseVelo() {
        actualTarget = launcherVelocity + INCREASE;
        return actualTarget;
    }

    // decrement
    public double decreaseVelo() {
        actualTarget = launcherVelocity - INCREASE;
        return actualTarget;
    }

    // determines which hive target to aim for based on what half of the field we are
    public Pose getTargetPose() {
        double Y;
        Pose targetPose;

        Y = robot.follower.pose().y();

        if (Y < TARGET_SWITCH_THRESHOLD) {
            targetPose = HIVE_TARGET1;
        } else {
            targetPose = HIVE_TARGET2;
        }

        return targetPose;
    }

    // gets our predicted pose by shifting the hive target by our velocity multiplied times our flight time (might have to make flight time a function we will see)
    public Pose getSotmTargetPose() {
        double Y, dx, dy;
        Pose sotmTargetPose;

        Y = robot.follower.pose().y();
        dx = getXVelo();
        dy = getYVelo();

        if (Y < TARGET_SWITCH_THRESHOLD) {
            sotmTargetPose = new Pose(
                HIVE_TARGET1.x() - dx * FLIGHT_TIME,
                HIVE_TARGET1.y() - dy * FLIGHT_TIME
            );
        } else {
            sotmTargetPose = new Pose(
                HIVE_TARGET2.x() - dx * FLIGHT_TIME,
                HIVE_TARGET2.y() - dy * FLIGHT_TIME
            );
        }

        return sotmTargetPose;
    }

    // takes our final turret angle does some math and returns a value in servo position aka 0-1
    public double getTurretPos() {
        double angle = getTurretAngle();
        double servoPos = TURRET_CENTER + angle / (2 * Math.PI);

        return clamp(servoPos, TURRET_MIN, TURRET_MAX);
    }

    public void setTurretTarget() {
        setTurretPosition(getTurretPos());
    }

    public void increaseVelocity() {
        launcherVelocity = increaseVelo();
    }

    public void decreaseVelocity() {
        launcherVelocity = decreaseVelo();
    }

    public void increaseHood() {
        manualHoodPos = manualHoodPos + HOOD_INCREMENT;
    }

    public void decreaseHood() {
        manualHoodPos = manualHoodPos - HOOD_INCREMENT;
    }

    // returns the target angle we want our hood to be at in degrees before we compensate for velocity
    public double getHoodTargetPos() {
        double x = hoodSOTM ? sotmDistanceToTarget : distanceToTarget;

        hoodTargetPos =
            HOOD_REGRESSION_A * Math.pow(x, 4) +
            HOOD_REGRESSION_B * Math.pow(x, 3) +
            HOOD_REGRESSION_C * Math.pow(x, 2) +
            HOOD_REGRESSION_D * x +
            HOOD_REGRESSION_E; // we run our distance into our regression formula

        return hoodTargetPos;
    }

    //returns the target servo position for our hood before we compensate for velocity
    /* public double getHoodTargetPos() {
        hoodTargetPos =
            HOOD_MAX - (hoodTargetAngle - HOOD_DOWN_ANGLE) / HOOD_POSITION_TO_ANGLE_CONSTANT; // basically shifting and scaling it to work

        return clamp(hoodTargetPos, HOOD_MIN, HOOD_MAX); // clamping it so the servo doesnt rebel from the rest of the robot
    } */

    // returns the final position in servo position (0-1) that we want our hood servo to be at after velocity compensation
    public double getHoodAutoPos() {
        hoodAutoPos =
            /*hoodTargetPos commented out for manual testing*/ hoodTargetPos +
            (error * hoodCompScalar) / HOOD_POSITION_TO_ANGLE_CONSTANT; // takes our target and subtracts our error times a scalar
        return clamp(hoodAutoPos, HOOD_MIN, HOOD_MAX); // clamp it again so it doesnt try and unionize (i think you only need to clamp it once but by doing it twice both the compensated and uncompensated values are actually usable)
    }

    public void setHoodAutoPos() {
        setHoodPos(hoodAutoPos);
    }

    @Override
    public void periodic() {
        targetServoPos = getTurretPos();
        targetPose = getTargetPose();
        turretHead = getTurretAngle();
        robotHead = robot.follower.pose().heading();
        targetVelo = getVelocityTarget();
        currentVelo = getActualVelocity();
        autoVelocity = getAutoVelocity();
        distanceToTarget = getDistance();
        hoodAutoPos = getHoodAutoPos();
        getHoodAutoPos();
        getHoodTargetPos();
        sotmDistanceToTarget = getSotmDistance();
        error = pidfController.getLastError();

        // Add an item to the array and update the index for the next update to the 'circular' array
        double power = pidfController.update(getActualVelocity());
        setLauncherPower(power);
    }

    private double getMotor1Current() {
        return hasHardware ? launcher.getAmperage(CurrentUnit.AMPS) : 0;
    }

    private double getActualVelocity() {
        if (hasHardware) {
            return launcher.getVelocity();
        } else {
            return pidfController.getTarget(); // Not a Number
        }
    }

    // takes distance and returns AUTO_VELOCITY by plugging it into the regression
    public double getAutoVelocity() {
        double x = velocitySOTM ? sotmDistanceToTarget : distanceToTarget;

        autoVelocity =
            LAUNCHER_REGRESSION_A * Math.pow(x, 4) +
            LAUNCHER_REGRESSION_B * Math.pow(x, 3) +
            LAUNCHER_REGRESSION_C * Math.pow(x, 2) +
            LAUNCHER_REGRESSION_D * x +
            //   LAUNCHER_REGRESSION_E; // comment when use SOTM
            LAUNCHER_REGRESSION_E_SOTM;
        return autoVelocity;
    }

    // Explicitly set the target velocity for the motors
    public void setVelocityTarget(double speed) {
        pidfController.setTarget(speed);
    }

    public void setTurretPosition(double pos) {
        turret1.setPosition(pos);
        turret2.setPosition(pos);
    }

    //gets our x velocity in in/s clamping it to not make the launcher have a seizure
    public double getXVelo() {
        xVelo = robot.follower.velocity().vx;
        return clamp(xVelo, -50, 50);
    }

    //gets our y velocity in in/s clamping it to not make the launcher have a seizure
    public double getYVelo() {
        yVelo = robot.follower.velocity().vy;
        return clamp(yVelo, -50, 50);
    }

    private void setHoodPos(double pos) {
        hood.setPosition(pos);
    }

    // Returns the current target velocity (which may be set explicitly, or automatically)
    public double getVelocityTarget() {
        return pidfController.getTarget();
    }

    private void setLauncherPower(double power) {
        if (hasHardware) {
            launcher.setPower(power);
        }
    }
}
