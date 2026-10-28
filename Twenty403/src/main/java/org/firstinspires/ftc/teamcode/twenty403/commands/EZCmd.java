package org.firstinspires.ftc.teamcode.twenty403.commands;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.twenty403.commands.driving.JoystickDriveCommand;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.LauncherSubsystem;

public class EZCmd {

    public static class Drive {

        public static Command ResetGyro(Follower follower) {
            return Command.create(() ->
                follower.setPose(new Pose(follower.pose().x(), follower.pose().y(), 0.0))
            );
        }
    }

    public static class Launcher {

        public static Command Launch(LauncherSubsystem launcher) {
            return Command.create(launcher::Launch);
        }

        public static Command Stop(LauncherSubsystem launcher) {
            return Command.create(launcher::Stop);
        }

        public static Command Reverse(LauncherSubsystem windmill) {
            return Command.create(windmill::Reverse);
        }
    }

    public static class PollenIntake {

        public static Command Intake(IntakeSubsystem intake) {
            return Command.create(intake::Intake);
        }

        public static Command Eject(IntakeSubsystem intake) {
            return Command.create(intake::Reject);
        }

        public static Command Stop(IntakeSubsystem intake) {
            return Command.create(intake::Stop);
        }
    }
}
