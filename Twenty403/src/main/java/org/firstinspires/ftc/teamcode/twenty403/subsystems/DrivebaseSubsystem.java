package org.firstinspires.ftc.teamcode.twenty403.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class DrivebaseSubsystem implements Subsystem, Loggable {

    @Log.Number(name = "X")
    public static double X;

    @Log.Number(name = "Y")
    public static double Y;

    @Log.Number(name = "H")
    public static double H;

    @Log.Boolean(name = "busy")
    public static boolean busy;

    private final Follower follower;
    private double fwdJoystick, strafeJoystick, rotateJoystick;
    private boolean autoAim = false;

    public DrivebaseSubsystem(Follower f) {
        follower = f;
        fwdJoystick = 0;
        strafeJoystick = 0;
        rotateJoystick = 0;
        CommandScheduler.register(this);
    }

    public void ResetGyro() {
        Pose p = follower.pose();
        follower.setPose(new Pose(p.x(), p.y(), 0));
    }

    public void setControllerInputs(double fwd, double strafe, double rot) {
        fwdJoystick = fwd;
        strafeJoystick = strafe;
        rotateJoystick = rot;
    }

    @Override
    public void periodic() {
        if (follower == null) {
            return;
        }
        Pose pose = follower.pose();
        X = fwdJoystick; //pose.x();
        Y = strafeJoystick; //pose.y();
        H = (pose.heading() * 180) / Math.PI;
        busy = follower.isBusy();
        if (!follower.isBusy()) {
            double rotate = getRotation();
            follower.manual(
                ManualDrive.fieldCentric(fwdJoystick, strafeJoystick, rotate, pose.heading())
            );
        }
        follower.update();
    }

    public void enableAutoAim() {
        autoAim = true;
    }

    public void disableAutoAim() {
        autoAim = false;
    }

    private double getRotation() {
        if (autoAim == false) {
            return rotateJoystick;
        }
        return 0.3;
    }
}
