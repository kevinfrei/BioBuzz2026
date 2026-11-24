package org.firstinspires.ftc.teamcode.learnbot.opmodes;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.jsonloader.PedroJsonLoader;

@Configurable
@TeleOp(name = "PedroVizTester")
public class PedroVizTester extends LinearOpMode {

    public TelemetryManager ptel;
    public PedroJsonLoader pedro;
    public static final String[] names = {
        "gardenLaunchToGarden",
        "gardenToGardenLaunch",
        "gardenLaunchToPark",
        "northStartToLaunch",
        "northLaunchToPark",
    };

    @Override
    public void runOpMode() throws InterruptedException {
        // During Init:
        ptel = PanelsTelemetry.INSTANCE.getTelemetry();
        pedro = new PedroJsonLoader(this.hardwareMap);
        ptel.addData("Pedro init", pedro.loaded);
        ptel.addLine(PedroJsonLoader.status);
        ptel.update(this.telemetry);
        waitForStart();
        while (opModeIsActive()) {
            ptel.addData("Pedro status", pedro.loaded);
            ptel.addData("Pedro other", PedroJsonLoader.other);
            ptel.addLine(PedroJsonLoader.status);
            for (String name : names) {
                ptel.addData(name, PedroJsonLoader.getCurve(name) != null ? "success" : "error");
            }
            ptel.update(this.telemetry);
        }
    }
}
