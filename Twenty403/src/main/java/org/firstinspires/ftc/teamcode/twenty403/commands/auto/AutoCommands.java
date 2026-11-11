package org.firstinspires.ftc.teamcode.twenty403.commands.auto;

import com.technototes.library.command.Command;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import org.firstinspires.ftc.teamcode.twenty403.Robot;

public class AutoCommands {

    public static Command Launch(Robot r) {
        return r.launcherSubsystem::Launch;
    }

    public static Command StopLaunch(Robot r) {
        return r.launcherSubsystem::Stop;
    }

    public static Command Intake(Robot r) {
        return r.intakeSubsystem::Intake;
    }

    public static Command StopIntake(Robot r) {
        return r.intakeSubsystem::Stop;
    }

    public static Command AutoFirePollen(Robot r) {
        return new SequentialCommandGroup(
            AutoCommands.Launch(r),
            new WaitCommand(1.5),
            AutoCommands.StopLaunch(r)
        );
    }
}
