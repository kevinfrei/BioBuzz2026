package org.firstinspires.ftc.teamcode.sixteen750.controls;

import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
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
    public CommandButton relocButton;
    public CommandButton holdButton;
    public CommandButton trackButton;
    public CommandButton flywheelButton;
    public CommandButton intakeTrigger;
    public CommandButton rampTrigger;
    public CommandButton rampUpButton;
    public CommandButton rampDownButton;
    public CommandButton rampAutoButton;
    public CommandButton veloUpButton;
    public CommandButton veloDownButton;
    public CommandButton hoodUpButton;
    public CommandButton hoodDownButton;
    public PedroDriver pedroDriver;
    public CommandButton hoodButton;

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
    }

    public void AssignNamedControllerButton() {
        resetGyroButton = gamepad.ps_options;
        driveLeftStick = gamepad.leftStick;
        driveRightStick = gamepad.rightStick;
        intakeTrigger = gamepad.rightTrigger.getAsButton();
        rampTrigger = gamepad.leftTrigger.getAsButton();

        veloUpButton = gamepad.dpadUp;
        veloDownButton = gamepad.dpadDown;
        hoodUpButton = gamepad.dpadRight;
        hoodDownButton = gamepad.dpadLeft;
        snailButton = gamepad.leftBumper;
        launchButton = gamepad.rightBumper;
        flywheelButton = gamepad.ps_circle;
        spitButton = gamepad.ps_square;
        // gateButton = gamepad.ps_cross;
        // holdButton = gamepad.ps_circle;
        relocButton = gamepad.ps_share;
        trackButton = gamepad.ps_triangle;
        hoodButton = gamepad.ps_cross;
        rampAutoButton = gamepad.dpadLeft;
        rampUpButton = gamepad.dpadUp;
        rampDownButton = gamepad.dpadDown;
    }

    public void bindDriveControls() {
        pedroDriver = new PedroDriver(
            robot.follower,
            driveLeftStick,
            driveRightStick,
            robot.limelightSubsystem
        );
        CommandScheduler.scheduleJoystick(pedroDriver);

        snailButton.whenPressedReleased(
            DrivingCommands.SnailDriving(pedroDriver),
            DrivingCommands.NormalDriving(pedroDriver)
        );

        resetGyroButton.whenPressed(DrivingCommands.ResetGyro(pedroDriver));
        relocButton.whenPressed(DrivingCommands.ResetPosition(pedroDriver));
    }

    public void bindLaunchControls() {
        trackButton.whileInverseToggled(TeleCommands.TrackHive(robot));
        flywheelButton.whileInverseToggled(TeleCommands.Launch(robot));
        hoodButton.whileInverseToggled(TeleCommands.AutoHood(robot));
       // veloUpButton.whenPressed(TeleCommands.IncreaseFlywheel(robot));
       // veloDownButton.whenPressed(TeleCommands.DecreaseFlywheel(robot));
       // hoodUpButton.whenPressed(TeleCommands.IncreaseHood(robot));
       // hoodDownButton.whenPressed(TeleCommands.DecreaseHood(robot));
    }

    public void bindIntakeControls() {
        rampUpButton.whenPressed(TeleCommands.RampIncrease(robot));
        rampDownButton.whenPressed(TeleCommands.RampDecrease(robot));
        rampAutoButton.whileInverseToggled(TeleCommands.RampAuto(robot));
        rampTrigger.whenPressedReleased(TeleCommands.RampDown(robot), TeleCommands.RampUp(robot));
        spitButton.whenPressed(TeleCommands.Spit(robot));
        spitButton.whenReleased(TeleCommands.Reject(robot));
        spitButton.whenReleased(TeleCommands.Hold(robot));
        intakeTrigger.whilePressed(TeleCommands.Intake(robot));
        intakeTrigger.whenReleased(TeleCommands.Reject(robot));
        intakeTrigger.whenReleased(TeleCommands.Hold(robot));
        launchButton.whilePressed(TeleCommands.Feed(robot));
        launchButton.whenReleased(TeleCommands.Hold(robot));
        //gateButton.whenPressedReleased(TeleCommands.GateOpen(robot), TeleCommands.GateClose(robot));
    }
}
