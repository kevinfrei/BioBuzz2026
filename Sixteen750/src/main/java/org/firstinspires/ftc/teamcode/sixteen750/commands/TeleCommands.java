package org.firstinspires.ftc.teamcode.sixteen750.commands;

import com.pedropathing.math.Pose;
import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;

public class TeleCommands {

    public static Command LLRelocCommand(Robot r) {
        return Command.create(new LLRelocCommand(r));
    }

    public static Command TurnTo90(Robot r) {
        return Command.create(() -> r.follower.hold(new Pose(90, 90, Math.toRadians(90)), false)); //pose might need to be current pose?
    }

    public static Command Intake(Robot r) {
        return Command.create(r.intakeSubsystem::Intake);
    }

    public static Command Feed(Robot r) {
        return Command.create(r.intakeSubsystem::Feed);
    }

    public static Command IntakeStop(Robot r) {
         return Command.create(r.intakeSubsystem::StopIntake);
    }

    public static Command Reject(Robot r) {
        return Command.create(r.intakeSubsystem::Reject);
    }

    public static Command Spit(Robot r) {
        return Command.create(r.intakeSubsystem::Spit);
    }

    public static Command Hold(Robot r) {
        return Command.create(r.intakeSubsystem::Hold);
    }

    public static Command GateOpen(Robot r) {
        return Command.create(r.intakeSubsystem::GateOpen);
    }

    public static Command GateClose(Robot r) {
        return Command.create(r.intakeSubsystem::GateClose);
    }
}
