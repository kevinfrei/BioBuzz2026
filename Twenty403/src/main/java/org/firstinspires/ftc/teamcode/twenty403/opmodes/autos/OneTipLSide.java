package org.firstinspires.ftc.teamcode.twenty403.opmodes.autos;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.jsonloader.PedroViz;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Robot;
import org.firstinspires.ftc.teamcode.twenty403.commands.PedroPathCommand;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.AutoCommands;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.AutoPaths;

@Autonomous(name = "OneTipChicago", preselectTeleOp = "2 Controller Drive \uD83D\uDDFF")
@SuppressWarnings("unused")
public class OneTipLSide extends CommandOpMode {

    public Robot robot;
    public Hardware hardware;
    public AutoPaths paths;
    private PanelsTelemetry panelsTelemetry;
    private TelemetryManager ptel;
    private Limelight3A limelight;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE);
        paths = new AutoPaths();
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        ptel = panelsTelemetry.getTelemetry();
        String loadStatus = PedroViz.LoadJSON(this.hardwareMap);
        ptel.addData("Pedro init", loadStatus == null ? "<null>" : loadStatus);
        ptel.addLine(PedroViz.status);
        ptel.update(this.telemetry);
        robot.follower.setPose(PedroViz.getCurve("NorthStartToLaunch").get(0));
        CommandScheduler.scheduleForState(
            new SequentialCommandGroup(
                new WaitCommand(14),
                new PedroPathCommand(robot.follower, PedroViz.getCurve("NorthStartToLaunch")),
                AutoCommands.AutoFirePollen(robot),
                new PedroPathCommand(robot.follower, PedroViz.getCurve("NorthLaunchToPark")),
                CommandScheduler::terminateOpMode
            ),
            OpModeState.RUN
        );
    }

    @Override
    public void runLoop() {}
}
