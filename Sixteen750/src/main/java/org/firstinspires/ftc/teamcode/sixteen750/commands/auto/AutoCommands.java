package org.firstinspires.ftc.teamcode.sixteen750.commands.auto;

import com.technototes.library.command.Command;
import com.technototes.library.command.ParallelRaceGroup;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.subsystems.IntakeSubsystem;

public class AutoCommands {
    public static SequentialCommandGroup Fire (Robot r) {
        return new SequentialCommandGroup(
                r.intakeSubsystem::Feed,
        new WaitCommand(1));
    }
    abstract static class AutoTurret implements Command {

        @Override
        public boolean isFinished() {
            return false;
        }

        public static Command AutoTrack(Robot r) {
            return Command.create(r.turretsubsystem::setTurretTarget);
        }
    }
    abstract static class AutoLaunch implements Command {

        @Override
        public boolean isFinished() {
            return false;
        }

        public static Command AutoVelo(Robot r) {
            return Command.create(r.turretsubsystem::Launch);
        }
    }
}
