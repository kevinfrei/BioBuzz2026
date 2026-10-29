package org.firstinspires.ftc.teamcode.sixteen750.opmodes.auto;

import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
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
import org.firstinspires.ftc.teamcode.sixteen750.commands.driving.DrivingCommands;
import org.firstinspires.ftc.teamcode.sixteen750.controls.DriverController;
import org.firstinspires.ftc.teamcode.sixteen750.helpers.StartingPosition;
import org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep.Paths;
import org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep.Poses;

@Autonomous(name = "DoublePark2Tip", preselectTeleOp = "MainTele")
@SuppressWarnings("unused")
public class DoublePark2Tip extends CommandOpMode {

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
                new PedroPathCommand(robot.follower, p.DStartToPartnerPark()),
                new PedroPathCommand(robot.follower, p.DPartnerParkToLaunch1()),
                new WaitCommand(0.8),
                a.Fire(robot),
                new PedroPathCommand(robot.follower, p.DLaunch1ToGardenPreInt()).alongWith(
                    t.Intake(robot)
                ),
                new PedroPathCommand(robot.follower, p.DGardenPreIntToGardenInt()).withTimeout(2),
                new WaitCommand(1.3),
                new PedroPathCommand(robot.follower, p.DGardenIntToLaunch2()),
                new WaitCommand(0.3),
                a.FireHalf(robot),
                new PedroPathCommand(robot.follower, p.DLaunch2ToLaunch3()),
                new WaitCommand(2),
                a.FireHalf(robot),
                new PedroPathCommand(robot.follower, p.DLaunch3ToNearFlowerPreInt()).alongWith(
                    t.Reject(robot),
                    t.RampDown(robot)
                ),
                new PedroPathCommand(robot.follower, p.DNearFlowerPreIntToNearFlowerInt())
                    .alongWith(t.Intake(robot))
                    .withTimeout(1.5),
                new WaitCommand(1.1),

                new PedroPathCommand(robot.follower, p.DNearFlowerIntToLaunch3()),
                new WaitCommand(0.5),
                a.Fire(robot),
                new PedroPathCommand(robot.follower, p.DLaunch3ToSideFlowerPreInt()).alongWith(
                    t.Intake(robot)
                ),
                new PedroPathCommand(
                    robot.follower,
                    p.DSideFlowerPreIntToSideFlowerInt()
                ).withTimeout(1.5),
                new WaitCommand(1.1),
                new PedroPathCommand(robot.follower, p.DSideFlowerIntToLaunch3()),
                new WaitCommand(0.3).alongWith(t.RampUp(robot)),
                a.Fire(robot),
                new PedroPathCommand(robot.follower, p.DLaunch3ToSidePark()),
                t.GateClose(robot),
                t.SaveEndOfAuto(robot),
                t.SaveEndHead(robot),
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
