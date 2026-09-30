package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
public class IntakeSubsystem implements Loggable, Subsystem {

    public static double INTAKE_VELOCITY = 1;

    public static double REJECT_VELOCITY = -0.4;

    public static double HOLD_VELOCITY = 0.4;

    public static double FEED_VELOCITY = 0.95;

    public static double BLOCK_POSITION = 0.5;

    public static double FIRE_POSITION = 0.7;

    boolean hasHardware;

    Motor<DcMotorEx> _intake;
    Motor<DcMotorEx> _transfer1, _transfer2;
    Servo _gate;

    public IntakeSubsystem(Hardware h) {
        hasHardware = Setup.Connected.INTAKESUBSYSTEM;
        // Do stuff in here
        if (hasHardware) {
            _intake = h.intake;
            _transfer1 = h.transfer1;
            _transfer2 = h.transfer2;
            _gate = h.gate;
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
    }

    public void Feed() {
        setIntakePower(-REJECT_VELOCITY);
        setTransferPower(FEED_VELOCITY);
        setGatePosition(FIRE_POSITION);
    }

    public void Reject() {
        setIntakePower(REJECT_VELOCITY);
    }

    public void Spit() {
        setIntakePower(-FEED_VELOCITY);
        setTransferPower(-FEED_VELOCITY);
    }

    public void Hold() {
        setTransferPower(HOLD_VELOCITY);
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

    private void setGatePosition(double pos) {
        if (hasHardware) {
            _gate.setPosition(pos);
        }
    }
}
