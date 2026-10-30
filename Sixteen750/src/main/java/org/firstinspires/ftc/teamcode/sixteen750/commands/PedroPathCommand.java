package org.firstinspires.ftc.teamcode.sixteen750.commands;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;
import com.technototes.library.command.Command;

public class PedroPathCommand implements Command {

    public Path pathChain;
    public Follower follower;
    public Pose begin;
    public double maxPowerScaling;
    public boolean currentPose;
    public boolean brake;

    public PedroPathCommand(Follower f, Path p, double maxPower) {
        follower = f;
        pathChain = p;
        currentPose = false;
        begin = null;
        brake = false;
    }

    public PedroPathCommand(Follower f, Path p, double maxPower, boolean b) {
        follower = f;
        pathChain = p;
        currentPose = false;
        begin = null;
        brake = b;
    }

    public PedroPathCommand(Follower f, Path p) {
        this(f, p, 0);
    }

    public PedroPathCommand(Follower f, Pose startPose, Path p, double maxPower) {
        follower = f;
        pathChain = p;
        currentPose = true;
        begin = startPose;
        maxPowerScaling = maxPower;
    }

    public PedroPathCommand(Follower f, Path p, boolean currPose, double maxPower) {
        follower = f;
        pathChain = p;
        currentPose = currPose;
        begin = null;
        maxPowerScaling = maxPower;
    }

    public PedroPathCommand(Follower f, Pose startPose, Path p) {
        this(f, startPose, p, 0);
    }

    public PedroPathCommand(Follower f, Path p, boolean currPose) {
        this(f, p, currPose, 0);
    }

    @Override
    public void initialize() {
        // I'm not sure we want to do this here...
        // follower.setMaxPowerScaling(Setup.OtherSettings.AUTO_SCALING);
        if (currentPose) {
            follower.setPose(begin == null ? follower.pose() : begin);
        }
        follower.follow(pathChain);
    }

    @Override
    public boolean isFinished() {
        return !follower.isBusy();
        // I *believe* that a properly tuned robot shouldn't need all this stuff
        /*
        if (
            follower.atParametricEnd() &&
            follower.getHeadingError() < follower.getCurrentPath().getPathEndHeadingConstraint()
        ) {
            return true;
        } else if (
            follower.getVelocity().getMagnitude() <
                follower.getCurrentPath().getPathEndVelocityConstraint() &&
            follower.getPose().distanceFrom(follower.getCurrentPath().endPose()) < 2.54 &&
            follower.getAngularVelocity() < 0.055
        ) {
            return true;
        } else {
            return false;
        }*/
    }

    @Override
    public void execute() {
        //follower.update(); // also additional calling of follower that we dont need since we have dt subsystem
    }
}
