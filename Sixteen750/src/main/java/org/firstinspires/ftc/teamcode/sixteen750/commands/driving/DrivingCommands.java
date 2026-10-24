package org.firstinspires.ftc.teamcode.sixteen750.commands.driving;

import com.pedropathing.math.Pose;
import com.technototes.library.command.Command;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;
import org.firstinspires.ftc.teamcode.sixteen750.commands.PedroDriver;

public class DrivingCommands {

    public static Command NormalDriving(PedroDriver pd) {
        return pd::SetNormalSpeed;
    }

    public static Command SnailDriving(PedroDriver pd) {
        return pd::SetSnailSpeed;
    }

    public static Command ResetPosition1(PedroDriver pd) {
        return pd::ResetPosition1;
    }

    public static Command ResetPosition2(PedroDriver pd) {
        return pd::ResetPosition2;
    }

    public static Command TurboDriving(PedroDriver pd) {
        return pd::SetTurboSpeed;
    }

    public static Command AutoOrient(PedroDriver pd) {
        return pd::EnableVisionDriving;
    }

    public static Command NoAutoOrient(PedroDriver pd) {
        return pd::EnableFreeDriving;
    }

    public static Command ResetGyro(PedroDriver pd) {
        return pd::ResetGyro;
    }

    public static Command SnapDriving(PedroDriver pd, double target) {
        return () -> pd.EnableSnapTargetDriving(target);
    }
}
