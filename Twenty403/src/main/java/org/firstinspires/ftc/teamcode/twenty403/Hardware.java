package org.firstinspires.ftc.teamcode.twenty403;

import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.motor.Motor;
import com.technototes.library.hardware.sensor.AdafruitIMU;
import com.technototes.library.hardware.sensor.IGyro;
import com.technototes.library.hardware.sensor.IMU;
import com.technototes.library.logger.Loggable;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;

public class Hardware implements Loggable {

    public static List<LynxModule> hubs;

    public EncodedMotor<DcMotorEx> launcher;
    public Motor<DcMotorEx> inTake;
    public Limelight3A limelight;
    public CRServo transferServo;
    public CRServo leftIntakeServo;
    public CRServo rightIntakeServo;
    public IGyro imu;

    /* Put other hardware here! */

    public Hardware(HardwareMap hwmap) {
        hubs = hwmap.getAll(LynxModule.class);
        if (Setup.Connected.DRIVEBASE) {
        }

        if (Setup.Connected.LAUNCHER) {
            launcher = new EncodedMotor<>(Setup.HardwareNames.LAUNCHER);
        }
        if (Setup.Connected.INTAKE) {
            leftIntakeServo = new CRServo(Setup.HardwareNames.LEFTINTAKESERVO);
            rightIntakeServo = new CRServo(Setup.HardwareNames.RIGHTINTAKESERVO);
        }
        if (Setup.Connected.LIMELIGHT) {
            limelight = hwmap.get(Limelight3A.class, Setup.HardwareNames.LIMELIGHT);
        }
        if (Setup.Connected.TRANSFER) {
            transferServo = new CRServo(Setup.HardwareNames.SPIN);
        }
        if (Setup.Connected.EXTERNALIMU) {
            imu = new AdafruitIMU(Setup.HardwareNames.EXTERNALIMU, AdafruitIMU.Orientation.Yaw);
        } else {
            imu = new IMU(
                Setup.HardwareNames.IMU,
                RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
            );
        }
    }

    // We can read the voltage from the different hubs for fun...
    public static double voltage() {
        double volt = 0;
        double count = 0;
        for (LynxModule lm : hubs) {
            count += 1;
            volt += lm.getInputVoltage(VoltageUnit.VOLTS);
        }
        return volt / count;
    }
}
