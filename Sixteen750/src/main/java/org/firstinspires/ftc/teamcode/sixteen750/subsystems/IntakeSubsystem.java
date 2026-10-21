package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
public class IntakeSubsystem implements Loggable, Subsystem {

    public static double INTAKE_VELOCITY = 1;

    public static double REJECT_VELOCITY = -0.4;
    public static double TRANSFER1_HOLD_VELOCITY = 0.65;
    public static double TRANSFER2_HOLD_VELOCITY = 0.5;
    public static double INTAKE_FEED_VELOCITY = 0.6;
    public static double TRANSFER1_FEED_VELOCITY = 1;
    public static double TRANSFER2_FEED_VELOCITY = 0.92;
    public static double BLOCK_POSITION = 0.53;
    public static double REGURGITATE_VELOCITY = -0.7;

    public static double FIRE_POSITION = 0.3;
    public static double RAMP_UP_POS = 0.28;
    public static double RAMP_DOWN_POS = 0.965;
    boolean hasHardware;

    Motor<DcMotorEx> _intake;
    Motor<DcMotorEx> _transfer1, _transfer2;
    Servo _gate, _ramp;

    public IntakeSubsystem(Hardware h) {
        hasHardware = Setup.Connected.INTAKESUBSYSTEM;
        // Do stuff in here
        if (hasHardware) {
            _intake = h.intake;
            _transfer1 = h.transfer1;
            _transfer2 = h.transfer2;
            _gate = h.gate;
            _ramp = h.ramp;
            CommandScheduler.register(this);
            _intake.setDirection(DcMotorSimple.Direction.FORWARD);
            _transfer1.setDirection(DcMotorSimple.Direction.FORWARD);
            _transfer2.setDirection(DcMotorSimple.Direction.FORWARD);
        } else {
            _intake = null;
            _transfer1 = null;
            _transfer2 = null;
        }
        // Create the array to hold past current values
    }

    public void Intake() {
        setIntakePower(INTAKE_VELOCITY);
        setGatePosition(BLOCK_POSITION);
    }

    public void Feed() {
        setIntakePower(INTAKE_FEED_VELOCITY); // Javier: minus reject? does that mean ingest, or does that mean that there's a sign problem?
        setTransfer1Power(TRANSFER1_FEED_VELOCITY);
        setTransfer2Power(TRANSFER2_FEED_VELOCITY);
        setGatePosition(FIRE_POSITION);
    }

    public void Reject() {
        setIntakePower(REJECT_VELOCITY);
    }

    public void Regurgitate() {
        setIntakePower(REGURGITATE_VELOCITY);
        setTransferPower(REGURGITATE_VELOCITY);
    }

    public void Hold() {
        setTransfer1Power(TRANSFER1_HOLD_VELOCITY);
        setTransfer2Power(TRANSFER2_HOLD_VELOCITY);
        setGatePosition(BLOCK_POSITION);
    }

    public void StopIntake() {
        setIntakePower(0);
        setTransferPower(0);
    }

    public void GateOpen() {
        setGatePosition(FIRE_POSITION);
    }

    public void GateClose() {
        setGatePosition(BLOCK_POSITION);
    }

    public void RampUp() {
        setRampPosition(RAMP_UP_POS);
    }

    public void RampDown() {
        setRampPosition(RAMP_DOWN_POS);
    }

    @Override
    public void periodic() {
        // Add an item to the array and update the index for the next update to the 'circular' array
    }

    private void setIntakePower(double power) {
        if (hasHardware) {
            _intake.setPower(power);
        }
    }

    private void setTransferPower(double power) {
        if (hasHardware) {
            _transfer1.setPower(power);
            _transfer2.setPower(power);
        }
    }

    private void setTransfer1Power(double power) {
        if (hasHardware) {
            _transfer1.setPower(power);
        }
    }

    private void setTransfer2Power(double power) {
        if (hasHardware) {
            _transfer2.setPower(power);
        }
    }

    private void setGatePosition(double pos) {
        if (hasHardware) {
            _gate.setPosition(pos);
        }
    }

    private void setRampPosition(double pos) {
        if (hasHardware) {
            _ramp.setPosition(pos);
        }
    }
}
