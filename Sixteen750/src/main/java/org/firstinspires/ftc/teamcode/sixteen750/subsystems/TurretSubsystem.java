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

    Gamepad gamepad;


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
            gamepad = null;
            launcher.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            launcher = null;
        }
        // Create the array to hold past current values
    }

    public void Launch() {
        // Spin the motors
        if (hasHardware) {
            launcher.setVelocity(2400);
        }
    }





    @Override
    public void periodic() {
        // Add an item to the array and update the index for the next update to the 'circular' array

    }
}
