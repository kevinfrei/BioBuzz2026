package org.firstinspires.ftc.teamcode.sixteen750;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.technototes.library.command.Command;
import com.technototes.library.logger.Loggable;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.sixteen750.helpers.StartingPosition;
import org.firstinspires.ftc.teamcode.sixteen750.subsystems.DriveBaseSubsystem;
import org.firstinspires.ftc.teamcode.sixteen750.subsystems.IntakeSubsystem;
import org.firstinspires.ftc.teamcode.sixteen750.subsystems.LimelightSubsystem;
import org.firstinspires.ftc.teamcode.sixteen750.subsystems.TurretSubsystem;

public class Robot implements Loggable {

    public StartingPosition position;
    public Alliance alliance;
    public static Pose endOfAutoPose;
    public static double endHead;
    public double initialVoltage;

    public IntakeSubsystem intakeSubsystem;
    public LimelightSubsystem limelightSubsystem;
    public TurretSubsystem turretsubsystem;
    public DriveBaseSubsystem driveBaseSubsystem;

    public Follower follower;
    private Hardware hardware;

    public Robot(Hardware hw, Alliance team, StartingPosition pos) {
        this.position = pos;
        this.alliance = team;
        this.hardware = hw;
        this.initialVoltage = hw.voltage();

        if (Setup.Connected.INTAKESUBSYSTEM) {
            this.intakeSubsystem = new IntakeSubsystem(hw);
        }
        if (Setup.Connected.LIMELIGHTSUBSYSTEM) {
            this.limelightSubsystem = new LimelightSubsystem(hw, team);
        }
        if (Setup.Connected.DRIVEBASE) {
            follower = PedroConstants.create(hw.map);
            this.driveBaseSubsystem = new DriveBaseSubsystem(follower);
        }

        if (Setup.Connected.TURRETSUBSYSTEM) {
            this.turretsubsystem = new TurretSubsystem(hw, this);
        }
    }

    public Hardware getHardware() {
        return hardware;
    }

    public Follower getFollower() {
        return follower;
    }

    public void prepForStart() {}

   public void saveEndHead() {
        Robot.endHead = follower.pose().heading();
   }
    public void saveEndOfAutoPose() {
        Robot.endOfAutoPose = follower.pose();
    }
}
