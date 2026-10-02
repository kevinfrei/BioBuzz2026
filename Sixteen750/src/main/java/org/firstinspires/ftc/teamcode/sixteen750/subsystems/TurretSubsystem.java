package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import static java.lang.Math.clamp;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
public class TurretSubsystem implements Loggable, Subsystem {
    Robot robot;
    Pose HIVE_TARGET1 = new Pose(58,56);
    Pose HIVE_TARGET2 = new Pose(58,85);

    public static double TARGET_SWITCH_THRESHOLD = 71;
    public static double TURRET_CENTER = 0.5;
    public static double TURRET_MIN = 0.1;
    public static double TURRET_MAX = 0.9;

    public static double ROTATE_LEFT = 0.15;
    public static double ROTATE_RIGHT = 0.85;

    public static double UP = 0.1;
    public static double DOWN = 0.5;

    public static double LAUNCHER_VELOCITY = -0.5;

    boolean hasHardware;

    EncodedMotor<DcMotorEx> launcher;
    Servo turret1, turret2, hood;

    public TurretSubsystem(Hardware h) {
        hasHardware = Setup.Connected.TURRETSUBSYSTEM;
        // Do stuff in here
        if (hasHardware) {
            launcher = h.launcher;
            turret1 = h.turret1;
            turret2 = h.turret2;
            hood = h.hood;
            CommandScheduler.register(this);
            launcher.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            launcher = null;
            turret1 = null;
            turret2 = null;
        }
        // Create the array to hold past current values
    }

    public void Launch() {
        // Spin the motors
        setLauncherVelocity(LAUNCHER_VELOCITY);
    }

    public void HoodUp() {
        setHoodPosition(UP);
    }

    public void HoodDown() {
        setHoodPosition(DOWN);
    }


    // takes the already decided upon target and takes the current robot pose and does some math to figure out what angle the turret needs to point to face the target i just guessed which direction is positive should be easy to flip
    public double getTurretAngle() {
        Pose TargetPose = getTargetPose();

        double X, Y, Head, dx, dy, FieldAngle, Angle;

        X = robot.follower.pose().x();
        Y = robot.follower.pose().y();
        Head = robot.follower.pose().heading();

        dx = TargetPose.x() - X;
        dy = TargetPose.y() - Y;
        FieldAngle = Math.atan2(dy, dx);

        Angle = (FieldAngle - Head);
        return Angle;
    }
    //determines the distance from the robot to the current hive target in inches to be used for hood angle and flywheel speed
    public double getDistance() {
        Pose RobotPose = robot.follower.pose();
        Pose TargetPose = getTargetPose();

        double Distance = RobotPose.distance(TargetPose);

        return Distance;
    }
    // determines which hive target to aim for based on what half of the field we are
    public Pose getTargetPose() {

        double Y;
        Pose  TargetPose;

        Y = robot.follower.pose().y();

        if (Y > TARGET_SWITCH_THRESHOLD) {
            TargetPose = HIVE_TARGET1;
        } else {
            TargetPose = HIVE_TARGET2;
        }

        return TargetPose;
    }
    // takes our turret target angle and turns it into a servo position also clamps it currently to not fry to wiring
    public double getTurretPos() {
        double Angle = getTurretAngle();
        double servoPos = TURRET_CENTER - (Angle / (2 * Math.PI));

        return clamp(servoPos, TURRET_MIN, TURRET_MAX);
    }
    public void setTurretTarget() {
        setTurretPosition(getTurretPos());
    }


    @Override
    public void periodic() {
        // Add an item to the array and update the index for the next update to the 'circular' array
    }

    private void setLauncherVelocity(double velo) {
        if (hasHardware) {
            launcher.setPower(0.8);
            //            launcher.setVelocity(velo);
        }
    }

    private void setHoodPosition(double pos) {
        if (hasHardware) {
            hood.setPosition(pos);
        }
    }

    private void setTurretPosition(double pos) {
        if (hasHardware) {
            turret1.setPosition(pos);
            turret2.setPosition(pos);
        }
    }
}
