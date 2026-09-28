package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.motor.MotorPlus;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
public class IntakeSubsystem implements Loggable, Subsystem {

    Gamepad gamepad;

    public static double INTAKE_VELOCITY = 1;

    public static double REJECT_VELOCITY = -0.4;

    public static double HOLD_VELOCITY = 0.4;

    public static double FEED_VELOCITY = 0.95;

    public static double BLOCK_POSITION = 0.5;

    public static double FIRE_POSITION = 0.7;

    boolean hasHardware;

    Motor<DcMotorEx> intake;
    Motor<DcMotorEx> transfer1, transfer2;
    Servo gate;

    public IntakeSubsystem(Hardware h) {
        hasHardware = Setup.Connected.INTAKESUBSYSTEM;
        // Do stuff in here
        if (hasHardware) {
            intake = h.intake;
            transfer1 = h.transfer1;
            transfer2 = h.transfer2;
            gate = h.gate;
            CommandScheduler.register(this);
            gamepad = null;
            intake.setDirection(DcMotorSimple.Direction.FORWARD);
            transfer1.setDirection(DcMotorSimple.Direction.FORWARD);
            transfer2.setDirection(DcMotorSimple.Direction.FORWARD);
        } else {
            intake = null;
        }
        // Create the array to hold past current values
    }

    public void Intake() {
        // Spin the motors
        if (hasHardware) {
            intake.setPower(INTAKE_VELOCITY);
        }
    }

    public void Feed() {
        if (hasHardware) {
            intake.setPower(-REJECT_VELOCITY);
            transfer1.setPower(FEED_VELOCITY);
            transfer2.setPower(FEED_VELOCITY);
            gate.setPosition(FIRE_POSITION);
        }
    }

    public void setGamepad(Gamepad g) {
        gamepad = g;
    }

    public void Reject() {
        if (hasHardware) {
            intake.setPower(REJECT_VELOCITY);
        }
    }

    public void Spit() {
        if (hasHardware) {
            intake.setPower(REJECT_VELOCITY);
            transfer1.setPower(REJECT_VELOCITY);
            transfer2.setPower(REJECT_VELOCITY);
        }
    }

    public void Hold() {
        if (hasHardware) {
            transfer1.setPower(HOLD_VELOCITY);
            transfer2.setPower(HOLD_VELOCITY);
            gate.setPosition(BLOCK_POSITION);
        }
    }

    public void StopIntake() {
        if (hasHardware) {
            intake.setPower(0);
            transfer1.setPower(0);
            transfer2.setPower(0);
        }
    }
    public void GateOpen() {
        if (hasHardware) {
            gate.setPosition(FIRE_POSITION);
        }
    }

    public void GateClose() {
        if (hasHardware) {
            gate.setPosition(BLOCK_POSITION);
        }
    }




    @Override
    public void periodic() {
        // Add an item to the array and update the index for the next update to the 'circular' array

    }
}
