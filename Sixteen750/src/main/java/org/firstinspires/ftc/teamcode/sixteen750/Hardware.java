package org.firstinspires.ftc.teamcode.sixteen750;

import com.bylazar.configurables.annotations.Configurable;
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
    public IGyro imu;
    public EncodedMotor<DcMotorEx> fl, fr, rl, rr;
    public Motor<DcMotorEx> intake;
    //public MotorPlus<DcMotorEx> intake2;
    public EncodedMotor<DcMotorEx> launcher1;

    public Servo gate;

    public Limelight3A limelight;

    public Servo cameraPitch;

    /* Put other hardware here! */

    public Hardware(HardwareMap hwmap) {
        map = hwmap;
        hubs = hwmap.getAll(LynxModule.class);

        imu = new IMU(
            Setup.HardwareNames.IMU,
            RevHubOrientationOnRobot.LogoFacingDirection.LEFT,
            RevHubOrientationOnRobot.UsbFacingDirection.UP
        );

        if (Setup.Connected.DRIVEBASE) {
            fl = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.FL_DRIVE_MOTOR);
            fr = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.FR_DRIVE_MOTOR);
            rl = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.RL_DRIVE_MOTOR);
            rr = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.RR_DRIVE_MOTOR);
        }

        if (Setup.Connected.INTAKESUBSYSTEM) {
            intake = new Motor<DcMotorEx>(Setup.HardwareNames.INTAKE_MOTOR);
        }
        if (Setup.Connected.LAUNCHERSUBSYSTEM) {
            launcher1 = new EncodedMotor<DcMotorEx>(Setup.HardwareNames.LAUNCHER_MOTOR);
            gate = new Servo(Setup.HardwareNames.HOOD_SERVO);
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
