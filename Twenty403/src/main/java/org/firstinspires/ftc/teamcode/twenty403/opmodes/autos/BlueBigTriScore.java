package org.firstinspires.ftc.teamcode.twenty403.opmodes.autos;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.command.SequentialCommandGroup;
import com.technototes.library.command.WaitCommand;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Robot;
import org.firstinspires.ftc.teamcode.twenty403.commands.FeedCMD;
import org.firstinspires.ftc.teamcode.twenty403.commands.auto.DriveAutoCommand;

@Disabled
@Autonomous(name = "BlueBigTriScore", preselectTeleOp = "Two Controller Drive \uD83D\uDDFF")
@SuppressWarnings("unused")
public class BlueBigTriScore extends CommandOpMode {

    public Robot robot;
    public Hardware hardware;

    // private Paths p;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.BLUE);
        robot.follower.update();
        CommandScheduler.register(robot.launcherSubsystem);
        CommandScheduler.scheduleForState(
            new SequentialCommandGroup(
                FeedCMD.Feed(robot),
                new DriveAutoCommand(robot.drivebaseSubsystem, -0.5, 0),
                new WaitCommand(.3),
                new DriveAutoCommand(robot.drivebaseSubsystem, 0, 0.5),
                new WaitCommand(.9),
                new DriveAutoCommand(robot.drivebaseSubsystem, 0, 0),
                CommandScheduler::terminateOpMode
            ),
            OpModeState.RUN
        );
    }

    @Override
    public void uponStart() {
        //        EZCmd.Drive.ResetGyro(robot.follower);
        robot.atStart();
    }

    public void end() {
        //        HeadingHelper.savePose(robot.follower.getPose());
    }
}
