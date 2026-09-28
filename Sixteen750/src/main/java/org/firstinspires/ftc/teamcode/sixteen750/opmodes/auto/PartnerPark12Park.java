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
import org.firstinspires.ftc.teamcode.sixteen750.pedro.
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;
import org.firstinspires.ftc.teamcode.sixteen750.commands.driving.DrivingCommands;
import org.firstinspires.ftc.teamcode.sixteen750.controls.DriverController;
import org.firstinspires.ftc.teamcode.sixteen750.controls.OperatorController;
import org.firstinspires.ftc.teamcode.sixteen750.helpers.StartingPosition;

@Autonomous(name = "BlueNear18Safe", preselectTeleOp = "BlueTele")
@SuppressWarnings("unused")
public class BlueNear18Safe extends CommandOpMode {

    public Robot robot;
    public DriverController controls;
    public Hardware hardware;
    public PedroDriver pedroDriver;
    private PanelsTelemetry panelsTelemetry;
    private Limelight3A limelight;

    private static Command BlueGateCycle1(Robot r) {
        return new SequentialCommandGroup(
                TeleCommands.Intake(r),
                new PedroPathCommand(r.follower, BPaths.SBLaunchToBGateInt1),
                new WaitCommand(1.1),
                new PedroPathCommand(r.follower, BPaths.SBGateInt1ToBLaunch),
                AutoCommands.AutoLaunching3Balls(r)
        );
    }

    private static Command BlueGateCycle2(Robot r) {
        return new SequentialCommandGroup(
                TeleCommands.Intake(r),
                new PedroPathCommand(r.follower, BPaths.SBLaunchToBGateInt2),
                new WaitCommand(1.1),
                new PedroPathCommand(r.follower, BPaths.SBGateInt2ToBLaunch),
                AutoCommands.AutoLaunching3Balls(r)
        );
    }

    // POSITION FOR COLIN:
    // X = 132.5 Y = 65.75 H = 41
    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE, StartingPosition.Net);
        BPaths p = new BPaths(robot.follower);
        TeleCommands t = new TeleCommands();
        AutoCommands a = new AutoCommands();
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        robot.follower.setStartingPose(Poses.StartPoses.getBStart());
        CommandScheduler.scheduleForState(
                new AltAutoVelocity(robot).alongWith(
                        new SequentialCommandGroup(
                                t.Launch(robot),
                                //TeleCommands.AutoLaunch1(robot),
                                t.GateUp(robot),
                                t.HoodUp(robot),
                                new PedroPathCommand(robot.follower, p.SBStartToBLaunch, p.power085),
                                a.AutoLaunching3Balls(robot),
                                new PedroPathCommand(robot.follower, p.SBLaunchToBInt1, p.power092).alongWith(
                                        t.Intake(robot)
                                ),
                                new PedroPathCommand(robot.follower, p.SBInt1ToBLaunch),
                                a.AutoLaunching3Balls(robot),
                                BlueGateCycle1(robot).alongWith(t.Intake(robot)),
                                new PedroPathCommand(robot.follower, p.SBLaunchToBInt2, p.power092).alongWith(
                                        t.Intake(robot)
                                ),
                                new PedroPathCommand(robot.follower, p.SBInt2ToBLaunch),
                                a.AutoLaunching3Balls(robot),
                                BlueGateCycle2(robot).alongWith(t.Intake(robot)),
                                new PedroPathCommand(robot.follower, p.SBLaunchToBInt3, p.power085).alongWith(
                                        t.Intake(robot)
                                ),
                                new PedroPathCommand(robot.follower, p.SBInt3ToBLaunch),
                                a.AutoLaunching3Balls(robot),
                                new PedroPathCommand(robot.follower, p.SBLaunchToBEnd),
                                t.StopLaunch(robot),
                                CommandScheduler::terminateOpMode
                        )
                ),
                OpModeState.RUN
        );
