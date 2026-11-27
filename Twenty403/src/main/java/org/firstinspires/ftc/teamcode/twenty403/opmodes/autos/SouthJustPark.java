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
import org.firstinspires.ftc.teamcode.twenty403.PedroConstants;
import org.firstinspires.ftc.teamcode.twenty403.Robot;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.DriveAutoCommand;
import org.firstinspires.ftc.teamcode.twenty403.controls.DriverController;

@Autonomous(name = "SouthPark", preselectTeleOp = "2 Controller Drive \uD83D\uDDFF")
@SuppressWarnings("unused")
public class SouthJustPark extends CommandOpMode {

    public Robot robot;
    public Hardware hardware;
    private PanelsTelemetry panelsTelemetry;
    private TelemetryManager ptel;
    private Limelight3A limelight;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE);
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        ptel = panelsTelemetry.getTelemetry();
        String loadStatus = PedroViz.LoadJSON(this.hardwareMap);
        ptel.addData("Pedro init", loadStatus == null ? "<null>" : loadStatus);
        ptel.addLine(PedroViz.status);
        ptel.update(this.telemetry);
        robot.follower.setPose(PedroViz.getPose("South"));
        CommandScheduler.scheduleForState(
            new SequentialCommandGroup(
                new DriveAutoCommand(robot.drivebaseSubsystem, 0.1, 0.5),
                new WaitCommand(.7),
                new DriveAutoCommand(robot.drivebaseSubsystem, 0, 0),
                CommandScheduler::terminateOpMode
            ),
            OpModeState.RUN
        );
    }

    @Override
    public void runLoop() {}
}
