package org.firstinspires.ftc.teamcode.twenty403.helpers;

import com.pedropathing.revhub.localizers.CustomIMU;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.technototes.library.hardware.sensor.AdafruitIMU;

public class AdafruitCustomIMU implements CustomIMU {

    private final AdafruitIMU imu;

    public AdafruitCustomIMU(AdafruitIMU i) {
        imu = i;
    }

    @Override
    public void initialize(HardwareMap hardwareMap, String hardwareMapName) {}

    @Override
    public double getHeading() {
        return imu.getHeading();
    }

    @Override
    public void resetYaw() {
        imu.setHeading(0);
    }
}
