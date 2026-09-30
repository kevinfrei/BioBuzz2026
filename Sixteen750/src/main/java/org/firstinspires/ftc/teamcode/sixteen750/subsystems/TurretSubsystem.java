package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
public class TurretSubsystem implements Loggable, Subsystem {

    public static double ROTATE_LEFT = 0.15;
    public static double ROTATE_RIGHT = 0.85;

    public static double UP = 0.1;
    public static double DOWN = 0.5;

    public static double LAUNCHER_VELOCITY = -0.5;
    public static double LAUNCHER_VELOCITY2 = 0.5;
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
        setLauncherVelocity(LAUNCHER_VELOCITY2);
    }

    public void HoodUp() {
        setHoodPosition(UP);
    }

    public void HoodDown() {
        setHoodPosition(DOWN);
    }

    public void TurretRotation() {
        setTurretPosition(ROTATE_LEFT);
        setTurretPosition(ROTATE_RIGHT);
    }

    @Override
    public void periodic() {
        // Add an item to the array and update the index for the next update to the 'circular' array
    }

    private void setLauncherVelocity(double velo) {
        if (hasHardware) {
            launcher.setVelocity(velo);
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
