package org.firstinspires.ftc.teamcode.learnbot.components;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple.Direction;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.MovingStatistics;
import com.technototes.library.command.Command;
import com.technototes.library.command.CommandScheduler;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.structure.ValidationOpMode;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.subsystem.TargetAcquisition;
import com.technototes.library.util.PIDFController;
import java.util.Locale;
import java.util.function.DoubleSupplier;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class Launcher {

    // *ALL* the configuration should go in here. I moved some things that had been constants up
    // to here, as they are "bot build configuration": are the motors reversed.
    @Configurable
    public static class Config {

        // This should be the motor that has the encoder
        public static String MotorName = "launcher";

        // Is the primary intake motor reversed?
        public static boolean ReverseMotor = false;

        // This should probably track the ReverseMotor value
        // The FeedFwd helper tell you the correct value.
        public static boolean ReverseEncoder = false;

        // The distance to use when auto-calculating velocity, but we don't have a target
        public static double DefaultDistance = 50;

        // The input value is the error of the target velocity that ranges from
        // -2800 to +2800 for a goBilda motor.
        // It's output is a power value in the -1 to +1 range.
        // So, P is probably in the range of .001-ish.
        // For a velocity-targeting PIDF, we probably want an I value, not a D value.
        public static PIDFCoefficients launchPID = new PIDFCoefficients(0.0, 0.0, 0.0, 0);

        // Stuff used for the Feed Forward function.
        // This one is highly variable, based on the amount of friction in the system
        public static double kStaticFriction = 0.54;
        public static double kDynamicFriction = 0.3;

        // This one tends to be somewhere between 0.0035 to 0.005 or so.
        public static double kVelocityConstant = 0.0044;

        // This one we have to measure acceleration rates in the tuner
        public static double kAccelerationConstant = 0.0001;

        // This is only used if we can't read the voltage.
        public static double DefaultVoltage = 12.0;

        // This is how much to add/subtract to *setMotorPower* when inc/dec velocity is invoked
        public static double PowerDelta = 0.025;

        // multiplier for x for launch speed formula
        public static double Regression_M = 6.261;
        // minimum velocity for launch speed formula
        public static double Regression_B = 1250;

        public static double CalcVelocity(double distInInches) {
            return Regression_M * distInInches + Regression_B;
        }

        public static double GetFrictionConstant(boolean inMotion) {
            return inMotion ? kDynamicFriction : kStaticFriction;
        }
    }

    // *All* commands for the subsystem belong in here. It's easy for the simple "call a method"
    // commands, but for more complicated commands, scroll down to see AutoVelocity/AutoVelocityImpl
    public static class Commands {

        // This is a little strange: It's a place to tuck away a reference to the Launcher Subsystem,
        // so that all the commands can get to it there.
        // It lets us do this:
        //    button.whenPressed(Launcher.Commands.IncreaseMotor());
        // Instead of this:
        //    button.whenPressed(Launcher.Commands.IncreaseMotor(r.launcherComponent));
        // This doesn't work if we have *two* different launchers, but I *think* that's unlikely in an
        // FTC game ;)
        private static Component component = null;

        public static void setComponent(Component c) {
            component = c;
        }

        // This command is a "while" thing: It sets it once.
        // If you want to keep it going, use AutoVelocity instead
        public static Command Launch() {
            return component::autoSetVelocityTarget;
        }

        public static Command StopLaunch() {
            return component::stop;
        }

        public static Command IncreaseVelocity() {
            return component::increasePower;
        }

        public static Command DecreaseVelocity() {
            return component::decreasePower;
        }

        // This is just to make all commands look the same to the 'outside' user:
        // You just call LauncherCommands.AutoVelocity() instead of needing to differentiate
        // between simple Command.create's and more complex "class" commands.
        public static Command AutoVelocity() {
            return new AutoVelocityImpl();
        }

        // This class is protected to ensure consistency: you don't use
        //   button.whenPressed(new AutoVelocityImpl());
        // but instead you have to use
        //   button.whenPressed(Launcher.Commands.AutoVelocity());
        protected static class AutoVelocityImpl implements Command {

            public AutoVelocityImpl() {}

            // This command is designed to *never* finish.
            // It should be run in a parallel command group/alongWith/raceWith group.
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public void execute() {
                component.autoSetVelocityTarget();
            }
        }
    }

    @Configurable
    public static class Component implements Loggable, Subsystem {

        @Log.Number(name = "Target Velocity")
        public double targetVelocity = 0.0;

        @Log.Number(name = "Smoothed Velocity Goal")
        public double smoothedVelocity = 0.0;

        @Log.Number(name = "Current Velocity")
        public double motorVelocity = 0.0;

        @Log.Number(name = "Target Power")
        public double targetPower = 0.0;

        @Log.Number(name = "Manual Extra Power")
        public double additionalAmount = 0.0;

        @Log.Number(name = "AutoAim Velocity")
        public double autoVelocity = 0.0;

        // This the PIDF controller that's used manage the power.
        // The PIDF values are set in the Config class above.
        private final PIDFController pidfController;
        public double targetAcceleration;
        private final ElapsedTime lastRead = new ElapsedTime();

        // External dependencies this component requires:
        // The two launcher motors:
        EncodedMotor<DcMotorEx> _launcher;
        // Interface to indicate position of a target
        TargetAcquisition _targetAcquisition;
        // Get the voltage, needed for a sensible FeedFwd function
        DoubleSupplier voltage;

        private static EncodedMotor<DcMotorEx> configMotor(String motorName, boolean rev) {
            return motorName == null
                ? null
                : new EncodedMotor<DcMotorEx>(motorName)
                      .setDirection(rev ? Direction.REVERSE : Direction.FORWARD)
                      .coast();
        }

        public Component(TargetAcquisition targetSubsystem, DoubleSupplier voltageSup) {
            _launcher = configMotor(Config.MotorName, Config.ReverseMotor);
            _targetAcquisition = targetSubsystem;
            voltage = () -> {
                double v = voltageSup != null ? voltageSup.getAsDouble() : Config.DefaultVoltage;
                return v > 0 ? v : Config.DefaultVoltage;
            };

            // A quick wander around google comes up with something like this for motor feedfwd:

            // launcherMyPID = new PIDFController(Config.launcherPID, target ->
            //    (frictionConstant + velocityConstant * target) / voltage);

            // The point is that motor RPM scales linearly with voltage, so to compensate, you should
            // divide by voltage: Don't try to scale something by a delta from peak. Just divide.

            // To solve that formula, get a fresh battery, run it at full power and measure the RPM.
            // (Well, and figure out kStaticFriction, too: The lowest value that will still get the
            // launcher barely moving)

            // NOTE The FeedForward Helper opmode calculates these numbers for you automatically!
            pidfController = new PIDFController(
                Config.launchPID,
                target ->
                    (Math.signum(target) * // signum(<0) = -1, signum(>0) = 1, signum(0) = *0*
                        Config.GetFrictionConstant(getActualVelocity() != 0) +
                        Config.kVelocityConstant * target +
                        Config.kAccelerationConstant * targetAcceleration) /
                    voltage.getAsDouble()
            );

            setVelocityTarget(0);
            CommandScheduler.register(this);
            // Save this off for commands to use
            Commands.setComponent(this);
        }

        public Component() {
            this(null, null);
        }

        public Component(DoubleSupplier voltageSup) {
            this(null, voltageSup);
        }

        // Explicitly set the target velocity for the motors
        public void setVelocityTarget(double speed) {
            targetVelocity = speed;
            updateTargetValues();
        }

        // Returns the current target velocity (which may be set explicitly, or automatically)
        public double getVelocityTarget() {
            return targetVelocity;
        }

        // Set the velocity target based on the TargetAcquisition interface
        public void autoSetVelocityTarget() {
            setVelocityTarget(calculateVelocityTarget()); //change to auto aim velocity
        }

        public double getActualVelocity() {
            if (hasLaunch()) {
                return _launcher.getVelocity() * (Config.ReverseEncoder ? -1 : 1);
            } else {
                return Double.NaN; // Not a Number
            }
        }

        protected void setPower(double pow) {
            double power = Math.clamp(pow, -1, 1);
            targetPower = power;
            if (hasLaunch()) {
                _launcher.setPower(power);
            }
        }

        private final MovingStatistics current = new MovingStatistics(1);

        public double getMotorCurrent() {
            if (!hasLaunch()) {
                return 0;
            }
            current.add(_launcher.getAmperage(CurrentUnit.AMPS));
            return current.getMean();
        }

        public void stop() {
            setVelocityTarget(0);
        }

        public void increasePower() {
            // Spin the motors pid goes here
            additionalAmount += Config.PowerDelta;
        }

        public void decreasePower() {
            // Spin the motors pid goes here
            additionalAmount -= Config.PowerDelta;
        }

        // This reads the distance from the TargetAcquisition interface, then
        // uses the M/B values from Config to return the goal velocity.
        // We could add stuff to compensate for the robot velocity to better aim while in motion.
        public double calculateVelocityTarget() {
            // x = distance in inches
            double x = getTargetDistance();
            return Config.CalcVelocity(x);
        }

        // This lets the 'no hardware' or 'subsystem disabled' thing still work without a functional
        // target acquisition subsystem. If there is a TA subsystem, it uses that, otherwise it
        // uses Config.DefaultDistance.
        private double getTargetDistance() {
            if (_targetAcquisition != null) {
                return _targetAcquisition.getDistance();
            }
            return Config.DefaultDistance;
        }

        // This calculates what the highest achievable rate of acceleration is,
        // and smooths the PID target velocities to get to the actual target as quickly as possible.
        private void updateTargetValues() {}

        @Override
        public void periodic() {
            double delta = lastRead.seconds();
            lastRead.reset();
            // Update some values for logging:
            autoVelocity = calculateVelocityTarget();
            targetVelocity = getVelocityTarget();
            motorVelocity = getActualVelocity();
            double power = pidfController.update(motorVelocity);
            setPower(power + Math.copySign(additionalAmount, power));
        }

        boolean hasLaunch() {
            return _launcher != null;
        }
    }

    @SuppressWarnings("unused")
    @TeleOp(name = "Launcher Validation", group = "Launcher")
    public static class Validator extends ValidationOpMode {

        public Launcher.Component lc;

        @Override
        public void init() {
            super.init();
            lc = new Launcher.Component(this::getVoltage);
        }

        @Override
        public void loop() {
            super.loop();
            addLine(">>> Press left trigger for Launcher control");
            addLine(">>> Hit the dpad to stop");
            String res = "";
            double p1 = gamepad1.left_trigger;
            if (lc.hasLaunch()) {
                lc._launcher.setPower(p1);
                res += "lt " + p1;
            } else {
                res += "(no launcher) ";
            }
            res += String.format(
                Locale.ENGLISH,
                "Speed: %.2f, Current: %.2f",
                lc.getActualVelocity(),
                lc.getMotorCurrent()
            );
            addLine(res);
            if (anyDpadReleased()) {
                terminateOpModeNow();
            }
        }
    }

    @SuppressWarnings("unused")
    @TeleOp(name = "Launcher FeedFwd Helper", group = "Launcher")
    public static class FeedFwdHelper extends ValidationOpMode {

        enum State {
            MeasureStaticFriction,
            ValidateStaticFriction,
            MeasureDynamicFriction,
            ValidateDynamicFriction,
            DoneWithFriction,
            MeasureVelocity,
            DoneWithVelocity,
            StartAcceleration,
            MeasureAcceleration,
            FinishAcceleration,
            Testing,
            RunTest,
            Abort,
        }

        Launcher.Component lc = null;
        State state = State.MeasureStaticFriction;
        String extra = "";

        @Override
        public void init() {
            super.init();
            lc = new Launcher.Component(this::getVoltage);
            state = State.MeasureStaticFriction;
            lc.setPower(0);
            staticFriction = Config.kStaticFriction * 0.5;
        }

        double staticFriction = 0.005;
        double dynamicFriction = 0.005;
        double velocityConstant = 0;
        double accelerationConstant = 0;

        double frictionStep = 0.001;
        MovingStatistics velocityConstantStats = new MovingStatistics(50);
        double vel = 0;
        double peakVel = 0;
        double targetVelocity = 0;
        double measuredVelocity = 0;
        MovingStatistics error = new MovingStatistics(1000);
        ElapsedTime lastUpdate = new ElapsedTime();

        // Start slowly increasing power until we detect motion, then back off ever so slightly
        // We could try a more elaborate binary-search algorithm, but this works pretty reliably
        private State MeasureStaticFriction() {
            double v = getVoltage();
            double amps = lc.getMotorCurrent();
            double power = staticFriction / v;
            lc.setPower(power);
            addLine("Search for static friction constant...");
            addData("kStaticFriction", staticFriction);
            addData("Voltage", v);
            addData("Power", power);
            addLine("************");
            addLine("*** Please be patient");
            addLine("*** (press a button to abort)");
            addLine("*** Dpad up to add 0.1");
            addLine("*** Dpad right to add 0.01");
            addLine("************");
            if (lastUpdate.milliseconds() >= 50) {
                lastUpdate.reset();
                // We update every 50 milliseconds, just to give it time to trigger the encoder
                measuredVelocity = lc.getActualVelocity();
                if (Math.abs(measuredVelocity) > 0.1) {
                    return State.ValidateStaticFriction;
                }
                staticFriction += frictionStep;
            }
            if (gamepad1.dpadUpWasReleased()) {
                staticFriction += 0.1;
            }
            if (gamepad1.dpadRightWasReleased()) {
                staticFriction += 0.01;
            }
            if (anyButtonsReleased()) {
                lc.setPower(0);
                return State.Abort;
            }
            return State.MeasureStaticFriction;
        }

        private State ValidateStaticFriction() {
            addLine("Validating observed kStaticFriction value");
            addData("Velocity", measuredVelocity);
            if (lastUpdate.milliseconds() >= 2000) {
                measuredVelocity = lc.getActualVelocity();
                // If it's still moving, we found the static friction value:
                if (Math.abs(measuredVelocity) > 1) {
                    // Set the dynamicFriction value for the next step...
                    dynamicFriction = staticFriction;
                    staticFriction -= frictionStep;
                    Config.ReverseEncoder = measuredVelocity < 0 != Config.ReverseEncoder;
                    extra = Config.ReverseEncoder
                        ? "Set Config.ReverseEncoder to true!"
                        : "Set Config.ReverseEncoder to false!";
                    return State.MeasureDynamicFriction;
                } else {
                    // If it didn't keep moving, it was probably a fluke: Keep searching
                    return State.MeasureStaticFriction;
                }
            }
            return State.ValidateStaticFriction;
        }

        private State MeasureDynamicFriction() {
            double v = getVoltage();
            double amps = lc.getMotorCurrent();
            double power = dynamicFriction / v;
            lc.setPower(power);
            addLine("Search for dynamic friction constant...");
            addData("kStaticFriction", staticFriction);
            addData("kDynamicFriction", dynamicFriction);
            addData("Voltage", v);
            addData("Power", power);
            addLine("************");
            addLine("*** Please be patient");
            addLine("*** (press a button to abort)");
            addLine("*** Dpad up to subtract 0.1");
            addLine("*** Dpad right to subtract 0.01");
            addLine("************");
            if (lastUpdate.milliseconds() >= 500) {
                lastUpdate.reset();
                // We update every 500 milliseconds, to give the system time to halt
                double measuredVelocity = lc.getActualVelocity();
                if (measuredVelocity == 0 || dynamicFriction <= 0) {
                    dynamicFriction = Math.max(0, dynamicFriction);
                    return State.ValidateDynamicFriction;
                }
                dynamicFriction -= frictionStep;
            }
            if (gamepad1.dpadUpWasReleased()) {
                dynamicFriction -= 0.1;
            }
            if (gamepad1.dpadRightWasReleased()) {
                dynamicFriction -= 0.01;
            }

            if (anyButtonsReleased()) {
                lc.setPower(0);
                return State.Abort;
            }
            return State.MeasureDynamicFriction;
        }

        // TODO: This could be better tested...
        private State ValidateDynamicFriction() {
            addLine("Validating observed kDynamicFriction value");
            if (lastUpdate.milliseconds() >= 2000) {
                double measuredVelocity = lc.getActualVelocity();
                // If it has stopped moving, we found the dynamic friction value:
                if (measuredVelocity == 0) {
                    return State.DoneWithFriction;
                } else {
                    // If it didn't keep moving, it was probably a fluke: Keep searching
                    return State.MeasureDynamicFriction;
                }
            }
            return State.ValidateDynamicFriction;
        }

        private State DoneWithFriction() {
            // If we're here, the system started moving. Stop the motors and set kStaticFriction to
            // just below what was necessary to start the system moving.
            // Display results of Static Friction calculator & wait for user.
            addData("kStaticFriction-->>", staticFriction);
            addData("kDynamicFriction-->>", dynamicFriction);
            if (!extra.isBlank()) addLine(extra);
            addLine("************");
            addLine("*** Hit a button to begin velocity measurement");
            addLine("************");
            lastUpdate.reset();
            return anyButtonsReleased() || anyDpadReleased()
                ? State.MeasureVelocity
                : State.DoneWithFriction;
        }

        private State MeasureVelocity() {
            // We're measuring the kVelocityConstant:
            if (anyButtonsReleased()) {
                velocityConstant = velocityConstantStats.getMean();
                lc.setPower(0);
                return State.DoneWithVelocity;
            }
            lc.setPower(1);
            vel = lc.getActualVelocity();
            peakVel = Math.max(peakVel, vel);
            double vol = getVoltage();
            if (vel != 0) {
                // power = (kFriction + kVelocityConstant * RPM + motorAmperage * motorResistance) / v
                // So
                //   1 = (kF + kV * RPM + motorAmperage * motorResistance) / v;
                // solve for kV:
                //   kV = (v - kF - motorAmperage * motorResistance) / RPM
                velocityConstant = (vol - dynamicFriction) / vel;
                velocityConstantStats.add(velocityConstant);
            }
            addData("kStaticFriction!", staticFriction);
            addData("kDynamicFriction!", dynamicFriction);
            addData("Current kV", velocityConstant);
            addData("Average kV", velocityConstantStats.getMean());
            addData("Velocity", vel);
            addData("Voltage", vol);
            addData("Peak Velocity", peakVel);
            addLine("************");
            addLine("*** Press a button to stop velocity measurement");
            addLine("************");
            return State.MeasureVelocity;
        }

        private State DoneWithVelocity() {
            addData("kStaticFriction!", staticFriction);
            addData("kDynamicFriction!", dynamicFriction);
            addData("kVelocityConstant!", velocityConstant);
            if (!extra.isBlank()) addLine(extra);
            addLine("************");
            addLine("*** Press a button to continue on to acceleration profiling");
            addLine("************");
            if (anyButtonsReleased()) {
                // Update the values from the Velocity Constant calculator & go to testing
                targetVelocity = 0;
                error.clear();
                error.add(0);
                lastUpdate.reset();
                Config.kStaticFriction = staticFriction;
                Config.kDynamicFriction = dynamicFriction;
                Config.kVelocityConstant = velocityConstant;
                return State.StartAcceleration;
            } else {
                lc.setPower(0);
                return State.DoneWithVelocity;
            }
        }

        private static class AccData {

            public double volts, vel, acc;

            public AccData(double v, double velo, double a) {
                volts = v;
                vel = velo;
                acc = a;
            }
        }

        public static int TPS_COUNT = 4;
        private MovingStatistics tps; // ticks per second
        private AccData[] tps2; // ticks per second^2
        private int tps2Pos;
        private double lastVel = 0.0;

        private State StartAccelerationProfiling() {
            // This gets complicated: We need to measure acceleration and then
            // produce a linear regression of the data we get to come up with
            // what the 'ideal' aaceleration looks like for the mass we're spinning
            if (lc.getActualVelocity() != 0) {
                // We wait until the launcher is actually stopped
                return State.StartAcceleration;
            }
            lastUpdate.reset();
            tps = new MovingStatistics(TPS_COUNT);
            tps2 = new AccData[250];
            tps2Pos = 0;
            lastVel = 0;
            lc.setPower(1.0);
            return State.MeasureAcceleration;
        }

        private State MeasureAcceleration() {
            addLine("***************");
            addLine("*** Calculating Acceleration values");
            addLine("***************");
            if (anyButtonsReleased()) {
                lc.setPower(0);
                return State.Abort;
            }
            // We're heading to full power, and measuring the tps2
            tps.add(lc.getActualVelocity());
            if (tps.getCount() != TPS_COUNT) {
                lastVel = Math.max(0, tps.getMean());
                lastUpdate.reset();
                return State.MeasureAcceleration;
            }
            double curVel = Math.max(0, tps.getMean());
            double acc = (curVel - lastVel) / lastUpdate.seconds();
            tps2[tps2Pos++] = new AccData(getVoltage(), curVel, acc);
            lastUpdate.reset();
            if (tps2Pos >= tps2.length || (tps2Pos > 20 && acc < 0.0001)) {
                return State.FinishAcceleration;
            }
            return State.MeasureAcceleration;
        }

        private double calculateKa() {
            double sumNumerator = 0.0; // Sum of (V_residual * alpha)
            double sumDenominator = 0.0; // Sum of (alpha^2)
            for (int i = 0; i < tps2Pos; i++) {
                AccData p = tps2[i];
                // Ignore points where acceleration is negative or zero (e.g., sensor noise at start)
                if (p.acc <= 100) continue;

                // Calculate V_residual: V_applied - Ks*sgn(v) - Kv*v
                double sgn = Math.signum(p.vel);
                double vResidual =
                    p.volts - (p.vel != 0 ? dynamicFriction * sgn + velocityConstant * p.vel : 0);

                sumNumerator += vResidual * p.acc;
                sumDenominator += p.acc * p.acc;
            }

            if (sumDenominator == 0) {
                return 0.0;
            }

            // Least squares slope: kA = sum(vResidual * alpha) / sum(alpha^2)
            return sumNumerator / sumDenominator;
        }

        private State FinishAcceleration() {
            if (anyButtonsReleased()) {
                return State.Testing;
            }
            accelerationConstant = calculateKa();
            addLine("************");
            addLine("* Acceleration calculation");
            addLine("************");
            addData("kAcceleration", accelerationConstant);
            return State.FinishAcceleration;
        }

        private State PrepTesting() {
            Config.kStaticFriction = staticFriction;
            Config.kDynamicFriction = dynamicFriction;
            Config.kVelocityConstant = velocityConstant;
            Config.kAccelerationConstant = accelerationConstant;
            targetVelocity = 0.0;
            return State.RunTest;
        }

        private State RunTesting() {
            // DoneWithVelocity sets the config values, so let's use the launcher's periodic
            // function to test the results.
            lc.setVelocityTarget(targetVelocity);
            if (lastUpdate.milliseconds() > 100) {
                lastUpdate.reset();
                vel = lc.getActualVelocity();
                error.add(targetVelocity - vel);
            }
            lc.periodic();
            addData("kStaticFriction", Config.kStaticFriction);
            addData("kDynamicFriction", Config.kDynamicFriction);
            addData("kVelocityConstant", Config.kVelocityConstant);
            addData("kAccelerationConstant", Config.kAccelerationConstant);
            addData("Current", lc.getMotorCurrent());
            addData("Voltage", getVoltage());
            addLine(
                String.format(
                    Locale.ENGLISH,
                    "Vel target: %.1f (actual: %.1f)",
                    targetVelocity,
                    vel
                )
            );
            addData("Power", lc.targetPower);
            if (!extra.isBlank()) addLine(extra);
            addLine(
                String.format(
                    Locale.ENGLISH,
                    "Error Mean %.2f, stdev %03f",
                    error.getMean(),
                    error.getStandardDeviation()
                )
            );
            addLine("************");
            addLine("*** Dpad Left/Right => Target +/-250");
            addLine("*** Dpad Up/Down => Target +/-20");
            addLine("*** PS5 triangle/X => dynamicFriction +/1 0.001");
            addLine("*** PS5 square/O => dynamicFriction +/1 0.01");
            addLine("************");
            if (gamepad1.dpadLeftWasPressed()) {
                lastUpdate.reset();
                targetVelocity -= 250;
            } else if (gamepad1.dpadRightWasPressed()) {
                lastUpdate.reset();
                targetVelocity += 250;
            } else if (gamepad1.dpadUpWasPressed()) {
                lastUpdate.reset();
                targetVelocity += 20;
                targetVelocity += 20;
            } else if (gamepad1.dpadDownWasPressed()) {
                lastUpdate.reset();
                targetVelocity -= 20;
            }
            if (gamepad1.triangleWasReleased()) {
                Config.kDynamicFriction += 0.001;
                targetVelocity -= 20;
            }
            if (gamepad1.triangleWasReleased()) {
                Config.kDynamicFriction += 0.001;
            }
            if (gamepad1.crossWasReleased()) {
                Config.kDynamicFriction -= 0.001;
            }
            if (gamepad1.squareWasReleased()) {
                Config.kDynamicFriction += 0.01;
            }
            if (gamepad1.circleWasReleased()) {
                Config.kDynamicFriction -= 0.01;
            }

            return State.RunTest;
        }

        public void loop() {
            // Look at that, a silly little state machine...
            switch (state) {
                case MeasureStaticFriction:
                    state = MeasureStaticFriction();
                    break;
                case ValidateStaticFriction:
                    state = ValidateStaticFriction();
                    break;
                case MeasureDynamicFriction:
                    state = MeasureDynamicFriction();
                    break;
                case ValidateDynamicFriction:
                    state = ValidateDynamicFriction();
                    break;
                case DoneWithFriction:
                    state = DoneWithFriction();
                    break;
                case MeasureVelocity:
                    state = MeasureVelocity();
                    break;
                case DoneWithVelocity:
                    state = DoneWithVelocity();
                    break;
                case StartAcceleration:
                    state = StartAccelerationProfiling();
                    break;
                case MeasureAcceleration:
                    state = MeasureAcceleration();
                    break;
                case FinishAcceleration:
                    state = FinishAcceleration();
                    break;
                case Testing:
                    state = PrepTesting();
                    break;
                case RunTest:
                    state = RunTesting();
                    break;
                case Abort:
                default:
                    terminateOpModeNow();
                    break;
            }

            super.loop();
        }
    }
}
