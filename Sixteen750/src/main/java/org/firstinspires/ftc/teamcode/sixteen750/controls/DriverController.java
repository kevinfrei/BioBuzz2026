package org.firstinspires.ftc.teamcode.sixteen750.controls;

import com.technototes.library.command.CommandScheduler;
import com.technototes.library.control.CommandAxis;
import com.technototes.library.control.CommandButton;
import com.technototes.library.control.CommandGamepad;
import com.technototes.library.control.Stick;
import com.technototes.library.logger.Loggable;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;
import org.firstinspires.ftc.teamcode.sixteen750.commands.PedroDriver;
import org.firstinspires.ftc.teamcode.sixteen750.commands.TeleCommands;
import org.firstinspires.ftc.teamcode.sixteen750.commands.driving.DrivingCommands;

public class DriverController implements Loggable {

    public Robot robot;
    public Hardware hardware;
    public CommandGamepad gamepad;

    public Stick driveLeftStick, driveRightStick;
    public CommandButton resetGyroButton;
    public CommandButton snailButton;
    public CommandButton launchButton;
    public CommandButton spitButton;
    public CommandButton gateButton;
    public CommandButton override;
    public CommandButton RelocButton;
    public CommandButton holdButton;
    public CommandButton turretlockButton;

    public CommandAxis intakeTrigger;
    public CommandAxis autoAim;
    public PedroDriver pedroDriver;

    public static double triggerThreshold = 0.1;

    public DriverController(CommandGamepad g, Robot r) {
        this.robot = r;
        gamepad = g;
        override = g.leftTrigger.getAsButton(0.5);
        override = g.rightTrigger.getAsButton(0.5);

        AssignNamedControllerButton();
        if (Setup.Connected.DRIVEBASE) {
            bindDriveControls();
        }
        if (Setup.Connected.TURRETSUBSYSTEM) {
            bindLaunchControls();
        }
        if (Setup.Connected.INTAKESUBSYSTEM) {
            bindIntakeControls();
        }

        if (Setup.Connected.AIMINGSUBSYSTEM) {
            bindAimControls();
        }
    }

    public void AssignNamedControllerButton() {
        resetGyroButton = gamepad.ps_options;
        driveLeftStick = gamepad.leftStick;
        driveRightStick = gamepad.rightStick;
        intakeTrigger = gamepad.rightTrigger;

        snailButton = gamepad.leftBumper;
        launchButton = gamepad.rightBumper;
        spitButton = gamepad.ps_square;
        gateButton = gamepad.ps_cross;
        holdButton = gamepad.ps_circle;
        RelocButton = gamepad.ps_share;
        turretlockButton = gamepad.dpadUp;
    }

    public void bindDriveControls() {
        pedroDriver = new PedroDriver(
            robot.follower,
            driveLeftStick,
            driveRightStick,
            robot.limelightSubsystem
        );
        CommandScheduler.scheduleJoystick(pedroDriver);

        // turboButton.whenPressed(DrivingCommands.TurboDriving(robot.drivebase));
        // turboButton.whenReleased(DrivingCommands.NormalDriving(robot.drivebase));
        snailButton.whenPressedReleased(
            DrivingCommands.SnailDriving(pedroDriver),
            DrivingCommands.NormalDriving(pedroDriver)
        );

        resetGyroButton.whenPressed(DrivingCommands.ResetGyro(pedroDriver));
        //MotorDecrease.whenPressed(TeleCommands.DecreaseMotor(robot));
        //MotorIncrease.whenPressed(TeleCommands.IncreaseMotor(robot));

       // if (Setup.Connected.LIMELIGHTSUBSYSTEM) {
          //  autoAim.whenPressed(DrivingCommands.AutoOrient(pedroDriver));
          //  autoAim.whenReleased(DrivingCommands.NoAutoOrient(pedroDriver));
          //  RelocButton.whenPressed(TeleCommands.LLRelocCommand(robot));
            //AltAutoAlign.whenPressed(new AltAutoOrient(robot));
            //AltAutoAlign.whenReleased(DrivingCommands.NormalDriving(pedroDriver));
       // }
        // autoAim.whilePressed(new LLPipelineChangeCommand(hardware.limelight, Setup.HardwareNames.AprilTag_Pipeline));
    }

    public void bindLaunchControls() {

    }

    public void bindIntakeControls() {
        spitButton.whenPressed(TeleCommands.Spit(robot));
        spitButton.whenReleased(TeleCommands.Reject(robot));
        spitButton.whenReleased(TeleCommands.Hold(robot));
        intakeTrigger.whilePressed(TeleCommands.Intake(robot));
        intakeTrigger.whenReleased(TeleCommands.Reject(robot));
        intakeTrigger.whenReleased(TeleCommands.Hold(robot));
        launchButton.whilePressed(TeleCommands.Feed(robot));
        launchButton.whenReleased(TeleCommands.Hold(robot));
        gateButton.whenPressedReleased(TeleCommands.GateOpen(robot), TeleCommands.GateClose(robot));
    }

    // spitTrigger.whilePressed(TeleCommands.Spit(robot.intakeSubsystem));
    // spitTrigger.whileReleased(TeleCommands.Intake(robot.intakeSubsystem));


    public void bindAimControls() {
        // if(yippee) {
        //     leverButton.whenPressed(
        //     TeleCommands.LeverStop(robot.aimingSubsystem));
        //     yippee = false;
        // } else {
        //     leverButton.whenPressed(
        //     TeleCommands.LeverGo(robot.aimingSubsystem));
        //     yippee = true;
        // }
        // gateButton.whenPressed(TeleCommands.GateDown(robot));
        gateButton.whenPressed(TeleCommands.Feed(robot));
        gateButton.whenReleased(TeleCommands.IntakeStop(robot));
        // gateButton.whenReleased(TeleCommands.GateUp(robot));

        //
        holdButton.whilePressed(TeleCommands.Intake(robot));
        // holdButton.whenPressed(TeleCommands.GateDown(robot));
        holdButton.whenReleased(TeleCommands.IntakeStop(robot));
        //holdButton.whenReleased(TeleCommands.GateUp(robot));
    }
}
