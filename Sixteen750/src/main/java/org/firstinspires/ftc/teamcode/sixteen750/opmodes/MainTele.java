package org.firstinspires.ftc.teamcode.sixteen750.opmodes;

import static org.firstinspires.ftc.teamcode.sixteen750.Setup.HardwareNames.AprilTag_Pipeline;

import com.bylazar.telemetry.PanelsTelemetry;
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
import org.firstinspires.ftc.teamcode.sixteen750.commands.driving.DrivingCommands;
import org.firstinspires.ftc.teamcode.sixteen750.controls.DriverController;
import org.firstinspires.ftc.teamcode.sixteen750.controls.OperatorController;
import org.firstinspires.ftc.teamcode.sixteen750.helpers.StartingPosition;

@TeleOp(name = "MainTele")
@SuppressWarnings("unused")
public class MainTele extends CommandOpMode implements Loggable {

    public Robot robot;
    public OperatorController controlsOperator;
    public DriverController controlsDriver;
    public Hardware hardware;
    private Limelight3A limelight;
    private PanelsTelemetry panelsTelemetry;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.RED, StartingPosition.Unspecified);
        // controlsOperator = new OperatorController(codriverGamepad, robot);
        panelsTelemetry = PanelsTelemetry.INSTANCE;
        robot.follower.setPose(new Pose(0, 0, 0));
        // limelight = hardwareMap.get(Limelight3A.class, Setup.HardwareNames.LIMELIGHT);
        controlsDriver = new DriverController(driverGamepad, robot);
        if (Setup.Connected.DRIVEBASE) {
            // Just pick a starting point
            CommandScheduler.scheduleForState(
                new SequentialCommandGroup(
                    HeadingHelper.RestorePreviousPosition(robot.follower),
                    DrivingCommands.ResetGyro(controlsDriver.pedroDriver),
                    Command.create(robot.turretsubsystem::Launch)
                ),
                OpModeState.INIT
            );
            // CommandScheduler.scheduleForState(
            //         TeleCommand aas.Intake(robot.intakeSubsystem),
            //         OpModeState.RUN
            // );
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
    }

    @Override
    public void runLoop() {}

    @Override
    public void end() {
        if (Setup.Connected.LIMELIGHTSUBSYSTEM) {
            limelight.stop();
        }
    }
}
