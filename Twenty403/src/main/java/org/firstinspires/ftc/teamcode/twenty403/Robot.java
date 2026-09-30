package org.firstinspires.ftc.teamcode.twenty403;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.twenty403.helpers.StartingPosition;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.twenty403.subsystems.LauncherSubsystem;

@Configurable
public class Robot implements Loggable {

    @Log(name = "H")
    public double gyro;

    @Log(name = "X")
    public double xv;

    @Log(name = "Y")
    public double yv;

    @Log(name = "R")
    public double rv;

    @Log(name = "Launcher")
    public double launcherVelociy;

    public StartingPosition position;
    public Alliance alliance;
    public double initialVoltage;

    public LauncherSubsystem launcherSubsystem;
    public IntakeSubsystem intakeSubsystem;
    public Follower follower;

    public Robot(Hardware hw, Alliance team, StartingPosition pos) {
        this.position = pos;
        this.alliance = team;
        this.initialVoltage = hw.voltage();
        if (Setup.Connected.LAUNCHER) {
            this.launcherSubsystem = new LauncherSubsystem(hw);
        }
        if (Setup.Connected.INTAKE) {
            this.intakeSubsystem = new IntakeSubsystem(hw);
        }
    }

    public void atStart() {}

    public Follower getFollower() {
        return follower;
    }
}
