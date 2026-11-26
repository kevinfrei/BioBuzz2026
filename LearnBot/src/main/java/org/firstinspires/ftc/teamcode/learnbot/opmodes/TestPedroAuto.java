package org.firstinspires.ftc.teamcode.learnbot.opmodes;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.technototes.library.structure.CommandOpMode;
import com.technototes.library.util.Alliance;
import org.firstinspires.ftc.teamcode.learnbot.Hardware;
import org.firstinspires.ftc.teamcode.learnbot.Robot;
import org.firstinspires.ftc.teamcode.learnbot.controls.DriverController;
import org.firstinspires.ftc.teamcode.learnbot.helpers.StartingPosition;

@SuppressWarnings("unused")
@Autonomous(name = "Test Pedro", preselectTeleOp = "Just Drive")
public class TestPedroAuto extends CommandOpMode {

    public Robot robot;
    public DriverController controls;
    public Hardware hardware;

    @Override
    public void uponInit() {
        hardware = new Hardware(hardwareMap);
        robot = new Robot(hardware, Alliance.RED, StartingPosition.Net);
        Follower f = hardware.follower;
        f.remainingDistance();
        f.completion();
        f.parametricCompletion();
        f.distanceToEndpoint();
        /*
        TestPaths p = new TestPaths(Pedro.getFollower());
        CommandScheduler.scheduleOnceForState(
            () -> Pedro.getFollower().setStartingPose(p.getStart()),
            OpModeState.INIT
        );
        CommandScheduler.scheduleForState(
            new SequentialCommandGroup(
                Pedro.Commands.FollowPath(p.Path1),
                Pedro.Commands.FollowPath(p.Path2),
                Pedro.Commands.FollowPath(p.Path3),
                Pedro.Commands.FollowPath(p.Path4),
                new WaitCommand(1),
                HeadingHelper.SaveCurrentPosition(Pedro.getFollower()),
                CommandScheduler::terminateOpMode
            ),
            OpModeState.RUN
        );
         */
    }

    public void uponStart() {
        robot.atStart();
    }

    public void end() {}
}
