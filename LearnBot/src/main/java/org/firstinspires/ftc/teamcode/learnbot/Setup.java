package org.firstinspires.ftc.teamcode.learnbot;

import com.bylazar.configurables.annotations.Configurable;

public class Setup {

    @Configurable
    public static class Connected {

        public static boolean DRIVEBASE = true;
        public static boolean LIMELIGHT = true;
        public static boolean PINPOINT = false;
        public static boolean OCTOQUAD = true;
        public static boolean GIMBAL = true;
        public static boolean LAUNCHER = false;
    }

    @Configurable
    public static class HardwareNames {

        public static String IMU = "imu";
        public static String LIMELIGHT = "limelight";
        public static String ALLIANCE_SWITCH_RED = "asr";
        public static String ALLIANCE_SWITCH_BLUE = "asb";
        public static String OCTOQUAD_MK2 = "octoquad";
        public static String YAW_SERVO = "yaw";
        public static String PITCH_SERVO = "pitch";
    }

    @Configurable
    public static class OtherSettings {

        public static int AUTOTIME = 25;
        public static double TRIGGER_THRESHOLD = 0.3;
    }
}
