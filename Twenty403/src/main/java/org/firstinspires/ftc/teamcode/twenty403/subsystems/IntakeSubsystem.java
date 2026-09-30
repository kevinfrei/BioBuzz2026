package org.firstinspires.ftc.teamcode.twenty403.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class IntakeSubsystem implements Subsystem {

    public static double INTAKE_SERVO_SPEED = 1;
    public static double REJECT_SERVO_SPEED = -0.5;

    public static double INTAKE_MOTOR_SPEED = 1;

    public static double REJECT_MOTOR_SPEED = -1;

    public IntakeSubsystem(Hardware h) {
        hasHardware = Setup.Connected.INTAKE;
        // Do stuff in here
        if (hasHardware) {
            _leftintake = h.leftIntakeServo;
            _rightintake = h.rightIntakeServo;
            _intakemotor = h.inTake;
        } else {
            _leftintake = null;
            _rightintake = null;
            _intakemotor = null;
        }
    }

    public void Intake() {
        // set the intake servo's power
        setIntakeServo(INTAKE_SERVO_SPEED);

        // set the intake motor's power

        setIntakeMotor(INTAKE_MOTOR_SPEED);
    }

    public void Reject() {
        // set the intake servo's power
        setIntakeServo(REJECT_SERVO_SPEED);

        // set the intake motor's power
        setIntakeMotor(REJECT_MOTOR_SPEED);
    }

    public void Stop() {
        // set the intake servo's power

        setIntakeServo(0);
        // set the intake motor's power
        setIntakeMotor(0);
    }

    // Hardware Interface
    boolean hasHardware;
    CRServo _leftintake, _rightintake;
    Motor<DcMotorEx> _intakemotor;

    private void setIntakeServo(double power) {
        if (hasHardware) {
            _leftintake.setPower(power);
            _rightintake.setPower(power);
        }
    }

    private void setIntakeMotor(double power) {
        if (hasHardware) {
            _intakemotor.setPower(power);
        }
    }
}
