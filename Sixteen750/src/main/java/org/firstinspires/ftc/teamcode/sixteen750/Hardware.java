package org.firstinspires.ftc.teamcode.sixteen750;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.sensor.IGyro;
import com.technototes.library.hardware.sensor.IMU;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Loggable;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

@Configurable
public class Hardware implements Loggable {

    public List<LynxModule> hubs;
    public HardwareMap map;
    public OctoQuad octoquad;
    public EncodedMotor<DcMotorEx> fl, fr, rl, rr;
    public Motor<DcMotorEx> intake;
    public Motor<DcMotorEx> transfer1;
    public Motor<DcMotorEx> transfer2;
    public Servo gate;
    public EncodedMotor<DcMotorEx> launcher;
    public Servo turret1, turret2;
    public Servo hood;
    public Limelight3A limelight;

    public Servo cameraPitch;

    /* Put other hardware here! */

    public Hardware(HardwareMap hwmap) {
        map = hwmap;
        hubs = hwmap.getAll(LynxModule.class);



        if (Setup.Connected.DRIVEBASE) {
            fl = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.FL_DRIVE_MOTOR);
            fr = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.FR_DRIVE_MOTOR);
            rl = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.RL_DRIVE_MOTOR);
            rr = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.RR_DRIVE_MOTOR);
        }

        if (Setup.Connected.INTAKESUBSYSTEM) {
            intake = new Motor<DcMotorEx>(Setup.HardwareNames.INTAKE_MOTOR);
            transfer1 = new Motor<DcMotorEx>(Setup.HardwareNames.TRANSFER_MOTOR1);
            transfer2 = new Motor<DcMotorEx>(Setup.HardwareNames.TRANSFER_MOTOR2);
            gate = new Servo(Setup.HardwareNames.GATE_SERVO);
        }
        if (Setup.Connected.TURRETSUBSYSTEM) {
            launcher = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.LAUNCHER_MOTOR);
            turret1 = new Servo(Setup.HardwareNames.TURRET_SERVO1);
            turret2 = new Servo(Setup.HardwareNames.TURRET_SERVO2);
            hood = new Servo(Setup.HardwareNames.HOOD_SERVO);
        }

        if (Setup.Connected.VISIONSUBSYSTEM) {
            limelight = hwmap.get(Limelight3A.class, Setup.HardwareNames.LIMELIGHT);
            cameraPitch = hwmap.get(Servo.class, Setup.HardwareNames.PITCH);
        }
    }

    // We can read the voltage from the different hubs for fun...
    public double voltage() {
        double volt = 0;
        double count = 0;
        for (LynxModule lm : hubs) {
            count += 1;
            volt += lm.getInputVoltage(VoltageUnit.VOLTS);
        }
        return volt / count;
    }
}
