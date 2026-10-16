package org.firstinspires.ftc.teamcode.sixteen750.opmodes.auto;

import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.ParallelCommandGroup;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.commands.PedroDriver;
import org.firstinspires.ftc.teamcode.sixteen750.commands.PedroPathCommand;
import org.firstinspires.ftc.teamcode.sixteen750.commands.TeleCommands;
import org.firstinspires.ftc.teamcode.sixteen750.commands.auto.AutoCommands;
import org.firstinspires.ftc.teamcode.sixteen750.controls.DriverController;
import org.firstinspires.ftc.teamcode.sixteen750.helpers.StartingPosition;
import org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep.Paths;
import org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep.Poses;

@Autonomous(name = "Solo1TipPark", preselectTeleOp = "MainTele")
@SuppressWarnings("unused")
public class Solo1TipPark extends CommandOpMode {

    public Robot robot;
    public DriverController controls;
    public Hardware hardware;
    public PedroDriver pedroDriver;
    private PanelsTelemetry panelsTelemetry;
    private Limelight3A limelight;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE, StartingPosition.Net);
        Paths p = new Paths();
        TeleCommands t = new TeleCommands();
        AutoCommands a = new AutoCommands();
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        robot.follower.setPose(Poses.StartPoses.getSideStart());
        CommandScheduler.scheduleForState(
                new SequentialCommandGroup(
                        new PedroPathCommand(robot.follower, p.SStartToLaunch()),
                        new WaitCommand(2),
                        a.Fire(robot),
                        new PedroPathCommand(robot.follower, p.SLaunchToGardenPreInt()).alongWith(t.Intake(robot)),
                        new WaitCommand(1),
                        new PedroPathCommand(robot.follower, p.SGardenPreIntToGardenInt()).withTimeout(2),
                        new WaitCommand(2),
                        new PedroPathCommand(robot.follower, p.SGardenIntToLaunch()),
                        new WaitCommand(.5),
                        a.Fire(robot),
                        new PedroPathCommand(robot.follower, p.SLaunchToPark()),
                        CommandScheduler::terminateOpMode
                ),
                OpModeState.RUN
        );
    }

    @Override
    public void runLoop() {
        robot.follower.update();
        robot.turretsubsystem.Launch();
        robot.turretsubsystem.setTurretTarget();
        robot.turretsubsystem.setHoodAutoPos();
    }
}
