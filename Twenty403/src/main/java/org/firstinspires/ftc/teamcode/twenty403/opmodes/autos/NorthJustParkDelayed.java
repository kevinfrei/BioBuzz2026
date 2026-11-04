package org.firstinspires.ftc.teamcode.twenty403.opmodes.autos;

import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.PedroConstants;
import org.firstinspires.ftc.teamcode.twenty403.Robot;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.DriveAutoCommand;
import org.firstinspires.ftc.teamcode.twenty403.controls.DriverController;

@Autonomous(name = "NorthParkDelay", preselectTeleOp = "2 Controller Drive \uD83D\uDDFF")
@SuppressWarnings("unused")
public class NorthJustParkDelayed extends CommandOpMode {

    public Robot robot;
    public Hardware hardware;
    private PanelsTelemetry panelsTelemetry;
    private Limelight3A limelight;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE);
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        robot.follower.setPose(PedroConstants.Poses.North);
        CommandScheduler.scheduleForState(
            new SequentialCommandGroup(
                new WaitCommand(5),
                new DriveAutoCommand(robot.drivebaseSubsystem, 0.1, -0.5),
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
