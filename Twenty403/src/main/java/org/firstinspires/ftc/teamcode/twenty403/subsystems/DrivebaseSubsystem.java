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
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class DrivebaseSubsystem implements Subsystem, Loggable {

    private final Follower follower;
    private double fwdJoystick, strafeJoystick, rotateJoystick;
    private double headingOffset;

    public DrivebaseSubsystem(Follower f) {
        follower = f;
        fwdJoystick = 0;
        strafeJoystick = 0;
        rotateJoystick = 0;
        headingOffset = 0;
        CommandScheduler.register(this);
    }

    public void ResetGyro() {
        headingOffset = follower.pose().heading();
    }

    public void setControllerInputs(double fwd, double strafe, double rot) {
        fwdJoystick = fwd;
        strafeJoystick = strafe;
        rotateJoystick = rot;
    }

    @Override
    public void periodic() {
        if (!follower.isBusy()) {
            Pose pose = follower.pose();
            follower.manual(fwdJoystick, strafeJoystick, rotateJoystick);
            ManualDrive.fieldCentric(
                fwdJoystick,
                strafeJoystick,
                rotateJoystick,
                pose.heading(),
                headingOffset
            );
        }
        follower.update();
    }
}
