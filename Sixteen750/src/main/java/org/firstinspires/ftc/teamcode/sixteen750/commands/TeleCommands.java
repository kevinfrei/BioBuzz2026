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

    /*
    public static Command Launch(Robot r) {
        return Command.create(r.launcherSubsystem::Launch);
    }

    public static Command SetFarShoot(Robot r) {
        return Command.create(r.launcherSubsystem::FarShoot);
    }

    public static Command SetCloseShoot(Robot r) {
        return Command.create(r.launcherSubsystem::CloseShoot);
    }

    public static Command AutoLaunch1(Robot r) {
        return Command.create(r.launcherSubsystem::AutoLaunch1);
    }

    public static Command AutoLaunch2(Robot r) {
        return Command.create(r.launcherSubsystem::AutoLaunch2);
    }

    public static Command FarAutoLaunch(Robot r) {
        return Command.create(r.launcherSubsystem::FarAutoLaunch);
    }

    public static Command StopLaunch(Robot r) {
        return Command.create(r.launcherSubsystem::Stop);
    }

    public static Command IdleLaunch(Robot r) {
        return Command.create(r.launcherSubsystem::Idle);
    }

    public static Command Rumble(Robot r) {
        return Command.create(r.limelightSubsystem::setRumble);
    }*/

    public static Command RumbleOff(Robot r) {
        return Command.create(r.limelightSubsystem::setRumbleOff);
    }

    /*
    public static Command IncreaseMotor(Robot r) {
        return Command.create(r.launcherSubsystem::IncreaseMotorVelocity);
    }

    public static Command DecreaseMotor(Robot r) {
        return Command.create(r.launcherSubsystem::DecreaseMotorVelocity);
    }
    */
    public static Command Intake(Robot r) {
        return Command.create(r.intakeSubsystem::Intake);
    }

    public static Command Feed(Robot r) {
        return Command.create(r.intakeSubsystem::Feed);
    }

    public static Command IntakeStop(Robot r) {
        return Command.create(r.intakeSubsystem::StopIntake);
    }



    public static Command Spit(Robot r) {
        return Command.create(r.intakeSubsystem::Spit);
    }

    public static Command HoldIntake(Robot r) {
        return Command.create(r.intakeSubsystem::Hold);
    }
}
