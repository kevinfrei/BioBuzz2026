package org.firstinspires.ftc.teamcode.sixteen750.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.jsonloader.PedroJsonLoader;

@TeleOp(name = "JsonTest")
@SuppressWarnings("unused")
public class JsonTestOpMode extends OpMode implements Loggable {

    public TelemetryManager ptel;
    public PedroJsonLoader jsonPaths;

    @Log.Number(name = "HZ")
    public double hertz = 0;

    private final ElapsedTime loopTimer = new ElapsedTime();
    IMU imu;

    @Override
    public void init() {
        ptel = PanelsTelemetry.INSTANCE.getTelemetry();
        jsonPaths = new PedroJsonLoader(hardwareMap);
        loopTimer.reset();
        imu = hardwareMap.get(IMU.class, "imu");
    }

    @Override
    public void loop() {
        YawPitchRollAngles ypr = imu.getRobotYawPitchRollAngles();
        double s = loopTimer.seconds();
        loopTimer.reset();
        hertz = 1.0 / s;
        ptel.addData("loop speed", hertz);
        ptel.addData("yaw", ypr.getYaw(AngleUnit.DEGREES));
        ptel.addData("pitch", ypr.getPitch(AngleUnit.DEGREES));
        ptel.addData("roll", ypr.getRoll(AngleUnit.DEGREES));
        ptel.addLine(PedroJsonLoader.other);
        ptel.addLine(PedroJsonLoader.status);
        ptel.update(telemetry);
    }
}
