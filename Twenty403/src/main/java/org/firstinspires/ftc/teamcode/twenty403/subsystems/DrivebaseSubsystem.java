package org.firstinspires.ftc.teamcode.twenty403.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.sun.tools.javac.jvm.Target;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.subsystem.TargetAcquisition;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class DrivebaseSubsystem implements Subsystem, Loggable {

    private final Follower follower;
    private double fwdJoystick, strafeJoystick, rotateJoystick;
    private double headingOffset;
    private double targetHeading;

    public DrivebaseSubsystem(Follower f) {
        follower = f;
        fwdJoystick = 0;
        strafeJoystick = 0;
        rotateJoystick = 0;
        headingOffset = 0;
        targetHeading = Math.PI;
        CommandScheduler.register(this);
    }

    public void ResetGyro() {
        targetHeading = follower.pose().heading();
    }

    public void setControllerInputs(double fwd, double strafe, double rot, double head) {
        fwdJoystick = fwd;
        strafeJoystick = strafe;
        rotateJoystick = rot;
        targetHeading = head;
    }

    @Override
    public void periodic() {
        if (!follower.isBusy()) {
            Pose pose = follower.pose();
            follower.manual(
                ManualDrive.fieldCentric(
                    fwdJoystick,
                    strafeJoystick,
                    rotateJoystick,
                    pose.heading(),
                    targetHeading
                )
            );
        }
        follower.update();
    }
}
