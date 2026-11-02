package org.firstinspires.ftc.teamcode.sixteen750.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import org.firstinspires.ftc.teamcode.jsonloader.PedroJsonLoader;

@TeleOp(name = "JsonTest")
@SuppressWarnings("unused")
public class JsonTestOpMode extends OpMode implements Loggable {

    public TelemetryManager ptel;
    public PedroJsonLoader jsonPaths;

    @Log.Number(name = "HZ")
    public double hertz = 0;

    private final ElapsedTime loopTimer = new ElapsedTime();

    @Override
    public void init() {
        ptel = PanelsTelemetry.INSTANCE.getTelemetry();
        jsonPaths = new PedroJsonLoader(hardwareMap);
    }

    @Override
    public void loop() {
        ptel.addLine(PedroJsonLoader.status);
        ptel.update(telemetry);
    }
}
