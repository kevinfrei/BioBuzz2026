package org.firstinspires.ftc.teamcode.twenty403.opmodes.autos;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.ParallelRaceGroup;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Robot;
import org.firstinspires.ftc.teamcode.twenty403.commands.PedroPathCommand;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.AutoCommands;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.AutoPaths;

@Configurable
@Autonomous(name = "TwoTipAuto", preselectTeleOp = "2 Controller Drive \uD83D\uDDFF")
@SuppressWarnings("unused")
public class TwoTipAuto extends CommandOpMode {

    public static double GARDEN_INTAKE_TIMEOUT = 7;
    public Robot robot;
    public Hardware hardware;
    public AutoPaths paths;
    private PanelsTelemetry panelsTelemetry;
    private Limelight3A limelight;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE);
        paths = new AutoPaths();
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        //robot.follower.setPose(PedroConstants.Poses.South);
        robot.follower.setPose(paths.pathStartToGardenLaunch().get(0));
        CommandScheduler.scheduleForState(
            new SequentialCommandGroup(
                new PedroPathCommand(robot.follower, paths.pathStartToGardenLaunch()),
                AutoCommands.AutoFirePollen(robot),
                AutoCommands.Intake(robot),
                new ParallelRaceGroup(
                    new SequentialCommandGroup(
                        new PedroPathCommand(robot.follower, paths.pathGardenLaunchToGarden()),
                        new WaitCommand(1.5),
                        AutoCommands.StopIntake(robot)
                    ),
                    new WaitCommand(GARDEN_INTAKE_TIMEOUT)
                ),
                new PedroPathCommand(robot.follower, paths.pathGardenToGardenLaunch()),
                AutoCommands.AutoFirePollen(robot),
                new PedroPathCommand(robot.follower, paths.pathGardenLaunchToPark()),
                CommandScheduler::terminateOpMode
            ),
            OpModeState.RUN
        );
    }

    @Override
    public void runLoop() {}
}
