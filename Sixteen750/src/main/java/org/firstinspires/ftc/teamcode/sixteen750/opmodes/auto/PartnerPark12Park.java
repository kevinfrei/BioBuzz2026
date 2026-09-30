package org.firstinspires.ftc.teamcode.sixteen750.opmodes.auto;

import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
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

@Autonomous(name = "PartnerPark12Park", preselectTeleOp = "BlueTele")
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
            new SequentialCommandGroup(
                //t.Launch(robot)                .alongWith(
                new PedroPathCommand(robot.follower, p.StartToPartnerPark()), //)
                new PedroPathCommand(robot.follower, p.PartnerParkToLaunch1()),
                //t.Feed(robot),
                new WaitCommand(0.6),
                new PedroPathCommand(robot.follower, p.Launch1ToGardenPreInt()),
                //.alongWith(                            t.Intake(robot)                    )
                new PedroPathCommand(robot.follower, p.GardenPreIntToGardenInt()),
                new WaitCommand(1),
                new PedroPathCommand(robot.follower, p.GardenIntToLaunch2()),
                //t.Feed(robot),
                new WaitCommand(0.6),
                new PedroPathCommand(robot.follower, p.Launch2ToPark()),
                CommandScheduler::terminateOpMode
            ),
            OpModeState.RUN
        );
    }
}
