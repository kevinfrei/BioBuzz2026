package org.firstinspires.ftc.teamcode.twenty403.commands.auto;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.DrivebaseSubsystem;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.LauncherSubsystem;

public class DriveAutoCommand implements Command {

    public DrivebaseSubsystem drivebase;
    double forward = 0;
    double strafe = 0;

    public DriveAutoCommand(DrivebaseSubsystem db, double fwd, double strf) {
        drivebase = db;
        forward = fwd;
        strafe = strf;
    }

    @Override
    public void execute() {
        drivebase.setControllerInputs(forward, strafe, 0);
    }
}
