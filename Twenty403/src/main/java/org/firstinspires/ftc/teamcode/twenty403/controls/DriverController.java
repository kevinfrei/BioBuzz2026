package org.firstinspires.ftc.teamcode.twenty403.controls;

import com.technototes.library.command.CommandScheduler;
import com.technototes.library.control.CommandAxis;
import com.technototes.library.control.CommandButton;
import com.technototes.library.control.CommandGamepad;
import com.technototes.library.control.Stick;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Robot;
import org.firstinspires.ftc.teamcode.twenty403.Setup;
import org.firstinspires.ftc.teamcode.twenty403.commands.EZCmd;
import org.firstinspires.ftc.teamcode.twenty403.commands.FeedCMD;
import org.firstinspires.ftc.teamcode.twenty403.commands.driving.JoystickDriveCommand;

public class DriverController {

    public Robot robot;
    public CommandGamepad gamepad;
    public Hardware hardware;

    public Stick driveLeftStick, driveRightStick;
    public CommandButton resetGyroButton;
    public CommandButton launch;
    public CommandButton intake;
    public CommandButton spitOut;
    public CommandButton launchFaster;
    public CommandButton launchSlower;

    public DriverController(CommandGamepad g, Robot r) {
        this.robot = r;
        gamepad = g;

        AssignNamedControllerButton();
        if (Setup.Connected.DRIVEBASE) {
            bindDriveControls();
        }
        if (Setup.Connected.LAUNCHER) {
            bindLaunchControls();
        }
        if (Setup.Connected.INTAKE) {
            bindIntakeControls();
        }
        if (Setup.Connected.LIMELIGHT) {
            bindPipelineControls();
        }
    }

    private void AssignNamedControllerButton() {
        resetGyroButton = gamepad.ps_options;
        driveLeftStick = gamepad.leftStick;
        driveRightStick = gamepad.rightStick;

        launch = gamepad.rightTrigger.getAsButton();
        intake = gamepad.leftTrigger.getAsButton();
        spitOut = gamepad.leftBumper;
        launchFaster = gamepad.dpadUp;
        launchSlower = gamepad.dpadDown;
    }

    public void bindDriveControls() {
        CommandScheduler.scheduleJoystick(
            new JoystickDriveCommand(robot.drivebaseSubsystem, driveLeftStick, driveRightStick)
        );
        resetGyroButton.whenPressed(robot.drivebaseSubsystem::ResetGyro);
    }

    public void bindLaunchControls() {
        if (Setup.Connected.LAUNCHER) {
            launch.whenPressed(EZCmd.Launcher.Launch(robot.launcherSubsystem));
        }
    }

    public void bindIntakeControls() {
        if (Setup.Connected.INTAKE) {
            intake.whenPressed(EZCmd.PollenIntake.Intake(robot.intakeSubsystem));
            spitOut.whenPressed(EZCmd.PollenIntake.Eject(robot.intakeSubsystem));
        }
    }

    public void bindPipelineControls() {
        //        pipelineMode.whenPressed(this::togglePipelineMode);
        //        if (pipelineToggle) {
        //            //            barcodePipeline.whenPressed(new LLPipelineChangeCommand(hardware.limelight, Setup.HardwareNames.Barcode_Pipeline));
        //            GreencolorPipeline.whenPressed(
        //                new LLPipelineChangeCommand(
        //                    hardware.limelight,
        //                    Setup.HardwareNames.Green_Color_Pipeline
        //                )
        //            );
        //            PurplecolorPipeline.whenPressed(
        //                new LLPipelineChangeCommand(
        //                    hardware.limelight,
        //                    Setup.HardwareNames.Purple_Color_Pipeline
        //                )
        //            );
        //            //            classifierPipeline.whenPressed(new LLPipelineChangeCommand(hardware.limelight, Setup.HardwareNames.Classifier_Pipeline));
        //            //            objectPipeline.whenPressed(new LLPipelineChangeCommand(hardware.limelight, Setup.HardwareNames.Object_Detection_Pipeline));
        //            apriltagPipeline.whenPressed(
        //                new LLPipelineChangeCommand(
        //                    hardware.limelight,
        //                    Setup.HardwareNames.AprilTag_Pipeline
        //                )
        //            );
        //        }
    }
}
