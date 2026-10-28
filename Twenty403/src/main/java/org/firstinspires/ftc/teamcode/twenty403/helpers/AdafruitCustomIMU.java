package org.firstinspires.ftc.teamcode.twenty403.helpers;

import com.pedropathing.revhub.localizers.CustomIMU;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.sensor.AdafruitIMU;

public class AdafruitCustomIMU implements CustomIMU {

    AdafruitIMU imu;

    @Override
    public void initialize(HardwareMap hardwareMap, String hardwareMapName) {
        imu = new AdafruitIMU(hardwareMapName, AdafruitIMU.Orientation.Yaw);
    }

    @Override
    public double getHeading() {
        return imu.getHeading();
    }

    @Override
    public void resetYaw() {
        imu.setHeading(0);
    }
}
