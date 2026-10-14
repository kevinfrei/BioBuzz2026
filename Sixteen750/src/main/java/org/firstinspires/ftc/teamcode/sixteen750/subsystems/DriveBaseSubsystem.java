package org.firstinspires.ftc.teamcode.sixteen750.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.sixteen750.Hardware;
import org.firstinspires.ftc.teamcode.sixteen750.Robot;

@Configurable
public class DriveBaseSubsystem implements Subsystem {

    public Follower follower;

    public DriveBaseSubsystem(Follower follower) {
        this.follower = follower;
        CommandScheduler.register(this);
    }

    @Override
    public void periodic() {
        this.follower.update();
    }
}
