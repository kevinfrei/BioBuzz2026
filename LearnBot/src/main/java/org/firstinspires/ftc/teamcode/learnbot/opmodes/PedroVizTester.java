package org.firstinspires.ftc.teamcode.learnbot.opmodes;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Utility;
import org.firstinspires.ftc.teamcode.jsonloader.PedroViz;

@Utility(name = "PedroVizTester")
public class PedroVizTester extends LinearOpMode {

    public TelemetryManager ptel;

    public static final String[] valueNames = {
        "xLeftEdge",
        "yNorthEdge",
        "xEdge",
        "yEdge",
        "left",
    };

    public static final String[] poseNames = {
        "gardenLaunch",
        "northStart",
        "northPark",
        "northLaunch",
        "northHive",
        "southHive",
        "gardenIntake",
        "southPark",
        "southLaunch",
        "southIntake",
        "loadZoneStart",
        "loadZonePark",
        "loadZoneLaunch",
        "loadZoneHive",
        "gardenHive",
        "gardenPark",
        "lzHive",
        "lzLaunch",
        "lzPark",
        "lzStart",
    };

    public static final String[] curveNames = {
        "gardenLaunchToGarden",
        "gardenToGardenLaunch",
        "gardenLaunchToPark",
        "northStartToLaunch",
        "northLaunchToPark",
        "southLaunchToGarden",
    };

    public static final String[] pathNames = {
        "northSingleVolley",
        "southLaunch",
        "loadZoneSingleVolley",
    };

    @Override
    public void runOpMode() throws InterruptedException {
        // During Init:
        ptel = PanelsTelemetry.INSTANCE.getTelemetry();
        String loadStatus = PedroViz.LoadJSON(this.hardwareMap);
        ptel.addData("Pedro init", loadStatus == null ? "<null>" : loadStatus);
        ptel.addLine(PedroViz.status);
        ptel.update(this.telemetry);
        waitForStart();
        while (opModeIsActive()) {
            ptel.addData("Pedro init", loadStatus == null ? "<null>" : loadStatus);
            ptel.addLine(PedroViz.status);
            ptel.addLine(" === Test failures below === ");
            for (String name : valueNames) {
                Double val = PedroViz.getValue(name);
                if (val == null) {
                    ptel.addData(name, "VALUE NOT FOUND");
                }
            }
            for (String name : poseNames) {
                Pose pose = PedroViz.getPose(name);
                if (pose == null) {
                    ptel.addData(name, "POSE NOT FOUND");
                }
            }
            for (String name : curveNames) {
                Path curve = PedroViz.getCurve(name);
                if (curve == null) {
                    ptel.addData(name, "CURVE NOT FOUND");
                }
            }
            for (String name : pathNames) {
                Path path = PedroViz.getPath(name);
                if (path == null) {
                    ptel.addData(name, "PATH NOT FOUND");
                }
            }
            ptel.update(this.telemetry);
        }
    }
}
