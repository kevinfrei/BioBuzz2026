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

@Autonomous(name = "PartnerPark12Park", preselectTeleOp = "MainTele")
@SuppressWarnings("unused")
public class PartnerPark12Park extends CommandOpMode {

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
        robot.follower.setPose(Poses.StartPoses.getStart());
        CommandScheduler.scheduleForState(
            new ParallelCommandGroup(
                t.Launch(robot),
                t.TrackHive(robot),
                t.AutoHood(robot)
            ).alongWith(
                new SequentialCommandGroup(
                    new PedroPathCommand(robot.follower, p.StartToPartnerPark()),
                    new WaitCommand(1),
                    new PedroPathCommand(robot.follower, p.PartnerParkToLaunch1()),
                    new WaitCommand(3),
                    t.Feed(robot),
                    new WaitCommand(1.2),
                    new PedroPathCommand(robot.follower, p.Launch1ToGardenPreInt()).alongWith(
                        t.Intake(robot)
                    ),
                    new WaitCommand(2),

                    new PedroPathCommand(robot.follower, p.GardenPreIntToGardenInt()),
                    new WaitCommand(1.5),
                    new PedroPathCommand(robot.follower, p.GardenIntToLaunch1()),
                    new WaitCommand(1),
                    t.Feed(robot),
                    new WaitCommand(1.2),
                    new PedroPathCommand(robot.follower, p.Launch1ToParkR()),
                    CommandScheduler::terminateOpMode
                )
            ),
            OpModeState.RUN
        );
    }

    @Override
    public void runLoop() {
        robot.follower.update();
    }
}
