package org.firstinspires.ftc.teamcode.twenty403.commands.driving;

import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.technototes.library.command.Command;
import com.technototes.library.control.CommandAxis;
import com.technototes.library.control.CommandButton;
import com.technototes.library.control.Stick;
import com.technototes.library.logger.Loggable;
import com.technototes.library.util.MathUtils;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import org.firstinspires.ftc.teamcode.twenty403.Setup;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.DrivebaseSubsystem;

public class JoystickDriveCommand implements Command {

    public DrivebaseSubsystem drivebase;
    public DoubleSupplier x, y, r;

    public JoystickDriveCommand(DrivebaseSubsystem drive, Stick xyStick, Stick rotStick) {
        x = xyStick.getXSupplier();
        y = xyStick.getYSupplier();
        r = rotStick.getXSupplier();
        drivebase = drive;
    }

    @Override
    public void execute() {
        double fwd = -y.getAsDouble();
        double strafe = -x.getAsDouble();
        double rotate = -r.getAsDouble();
        drivebase.setControllerInputs(fwd, strafe, rotate);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}
