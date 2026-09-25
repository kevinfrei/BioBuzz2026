package org.firstinspires.ftc.teamcode.learnbot;

import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.sensor.IGyro;
import com.technototes.library.hardware.sensor.IMU;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.logger.Loggable;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;
import org.firstinspires.ftc.teamcode.learnbot.Setup.HardwareNames;
import org.firstinspires.ftc.teamcode.learnbot.components.Gimbal;
import org.firstinspires.ftc.teamcode.learnbot.components.PedroDrivebase;
import org.firstinspires.ftc.teamcode.learnbot.subsystems.AllianceDetection;

public class Hardware implements Loggable {

    public List<LynxModule> hubs;
    public HardwareMap map;
    public IGyro imu;
    public AllianceDetection allianceDetection;
    public Limelight3A limelight;
    public Follower follower;
    public Servo yawServo, pitchServo;

    /* Put other hardware here! */

    public Hardware(HardwareMap hwmap) {
        map = hwmap;
        hubs = hwmap.getAll(LynxModule.class);
        imu = new IMU(
            HardwareNames.IMU,
            RevHubOrientationOnRobot.LogoFacingDirection.FORWARD,
            RevHubOrientationOnRobot.UsbFacingDirection.UP
        );
        if (Setup.Connected.DRIVEBASE) {
            follower = PedroDrivebase.createFollower(hwmap);
        }
        if (Setup.Connected.LIMELIGHT) {
            limelight = hwmap.get(Limelight3A.class, HardwareNames.LIMELIGHT);
        }
        if (Setup.Connected.GIMBAL) {
            yawServo = new Servo(Gimbal.Config.YAW_SERVO);
            pitchServo = new Servo(Gimbal.Config.PITCH_SERVO);
        }
        allianceDetection = new AllianceDetection(
            hwmap,
            HardwareNames.ALLIANCE_SWITCH_RED,
            HardwareNames.ALLIANCE_SWITCH_BLUE
        );
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
