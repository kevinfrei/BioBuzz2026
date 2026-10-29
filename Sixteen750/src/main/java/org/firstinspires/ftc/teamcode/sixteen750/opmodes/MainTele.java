package org.firstinspires.ftc.teamcode.sixteen750.opmodes;

import static org.firstinspires.ftc.teamcode.sixteen750.Setup.HardwareNames.AprilTag_Pipeline;
import static org.firstinspires.ftc.teamcode.sixteen750.pedro.meepmeep.Paths.follower;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.logger.Loggable;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import com.technototes.library.util.HeadingHelper;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;
import org.firstinspires.ftc.teamcode.sixteen750.commands.PedroDriver;
import org.firstinspires.ftc.teamcode.sixteen750.commands.driving.DrivingCommands;
import org.firstinspires.ftc.teamcode.sixteen750.controls.DriverController;
import org.firstinspires.ftc.teamcode.sixteen750.controls.OperatorController;
import org.firstinspires.ftc.teamcode.sixteen750.helpers.StartingPosition;
import org.firstinspires.ftc.teamcode.sixteen750.subsystems.TurretSubsystem;

@TeleOp(name = "MainTele")
@SuppressWarnings("unused")
public class MainTele extends CommandOpMode implements Loggable {

    public Robot robot;
    public OperatorController controlsOperator;
    public DriverController controlsDriver;
    public Hardware hardware;
    private Limelight3A limelight;
    public TelemetryManager ptel;
    public PedroDriver pd;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.RED, StartingPosition.Unspecified);
        // controlsOperator = new OperatorController(codriverGamepad, robot);
        robot.follower.setPose(new Pose(6.75, 6.75, 0)); // temporary starting pos for now
        // limelight = hardwareMap.get(Limelight3A.class, Setup.HardwareNames.LIMELIGHT);
        controlsDriver = new DriverController(driverGamepad, robot);
        if (Setup.Connected.DRIVEBASE) {
            // Just pick a starting point
            CommandScheduler.scheduleForState(
                new SequentialCommandGroup(
                    HeadingHelper.RestorePreviousPosition(robot.follower),
                    DrivingCommands.ResetGyro(controlsDriver.pedroDriver)
                ),
                OpModeState.INIT
            );
            // CommandScheduler.scheduleForState(
            //         TeleCommand aas.Intake(robot.intakeSubsystem),
            //         OpModeState.RUN
            // );
            ptel = PanelsTelemetry.INSTANCE.getTelemetry();
        }
        if (Setup.Connected.LIMELIGHTSUBSYSTEM) {
            limelight = hardware.limelight;
            limelight.setPollRateHz(100);

            telemetry.setMsTransmissionInterval(11);

            limelight.pipelineSwitch(AprilTag_Pipeline);
            CommandScheduler.register(robot.limelightSubsystem);

            /*
             * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
             */
            limelight.start();
        }
        if (Setup.Connected.TURRETSUBSYSTEM) {
            // CommandScheduler.register(robot.launcherSubsystem);
        }
    }

    @Override
    public void uponStart() {
        robot.prepForStart();
        if (Robot.endOfAutoPose != null) {
            robot.follower.setPose(Robot.endOfAutoPose);
            robot.follower.setHeading(Robot.endHead);
        }
    }

    @Override
    public void runLoop() {
        robot.follower.update();
        ptel.addData("targetvelo", TurretSubsystem.targetVelo);
        ptel.addData("currentvelo", TurretSubsystem.currentVelo);
        ptel.addData("Err", TurretSubsystem.error);
        ptel.update();
    }

    @Override
    public void end() {
        if (Setup.Connected.LIMELIGHTSUBSYSTEM) {
            limelight.stop();
        }
    }
}
