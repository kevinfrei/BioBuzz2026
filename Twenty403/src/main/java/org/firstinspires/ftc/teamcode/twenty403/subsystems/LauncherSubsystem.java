package org.firstinspires.ftc.teamcode.twenty403.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.MovingStatistics;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.util.PIDFController;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class LauncherSubsystem implements Loggable, Subsystem {

    public static double TARGET_MOTOR_VELOCITY = 1275; //.58; // 0.5 // /1.0
    public static double WINDMILL_SPIN = -0.15;
    public static double REVERSE_WINDMILL = 0.15;
    public static double Target_windmill = 250;
    // GoBilda says stall current of 9.2A at 12V, so V = I * R, R = 12 / 9.2 (about 1.3 ohms)
    // As the motor heats up, resistance also increase, so we could increase this a little bit
    // or maybe increase it over time to counteract that, but this is probably good enough.
    public static double MotorResistance = 0; //12 / 9.2;
    public static double kVelocityConstant = 0.0043;
    public static double kDynamicFriction = 0.22;
    public static double kStaticFriction = 0.27;
    public static int AMP_MOTOR_SAMPLE = 4;

    boolean hasHardware;
    public static EncodedMotor<DcMotorEx> launchMotor;
    public CRServo windmill;
    // Reverse encoder, Reverse motor
    // this doesn't really matter as much since f calculation is just overriding it :(
    public static PIDFCoefficients launcherP = new PIDFCoefficients(0.0005, 0.00005, 0.0, 0);
    public static double SPIN_F_SCALE = 0.00016;
    public static double SPIN_VOLT_COMP = 0.0216;
    public static double DIFFERENCE = 0.0046;
    public static double PEAK_VOLTAGE = 13;
    public static boolean ISLAUNCHMOTORREVERSED = true;
    private static PIDFController launcherPID;

    public static double GetFrictionConstant(boolean inMotion) {
        return inMotion ? kDynamicFriction : kStaticFriction;
    }

    private double voltage;

    @Log(name = "Launcher Velo: ")
    public static double MOTOR_VELOCITY;

    @Log(name = "Target Speed: ")
    public static double target;

    @Log(name = "Launcher Power: ")
    public static double power;

    @Log(name = "Err")
    public static double err;

    private boolean launching = false;
    private boolean reverse = false;

    public LauncherSubsystem(Hardware h) {
        hasHardware = Setup.Connected.LAUNCHER;
        // Do stuff in here
        if (hasHardware) {
            launchMotor = h.launcher;
            launchMotor.coast();
            if (ISLAUNCHMOTORREVERSED) {
                launchMotor.setDirection(DcMotorSimple.Direction.REVERSE);
            } else {
                launchMotor.setDirection(DcMotorSimple.Direction.FORWARD);
            }

            double ADDITION = PEAK_VOLTAGE - h.voltage();
            if (ADDITION == 0) {
                SPIN_VOLT_COMP = SPIN_VOLT_COMP + 0.001;
            } else {
                SPIN_VOLT_COMP = SPIN_VOLT_COMP + ADDITION * DIFFERENCE;
            }
            launcherPID = new PIDFController(
                launcherP,
                target ->
                    (Math.signum(target) * // signum(<0) = -1, signum(>0) = 1, signum(0) = *0*
                        (GetFrictionConstant(getMotorSpeed() != 0) +
                            getMotor1Current() * MotorResistance) +
                        kVelocityConstant * target) /
                    Hardware.voltage()
            );
            //            top.setPIDFCoefficients(launcherP);
            setTargetSpeed(0);

            // set the windmill
            windmill = h.windmill;
            setWindmillPower(0);
        } else {
            launchMotor = null;
        }
    }

    public void Launch() {
        // Spin the motors
        // TODO: make the motors spit the thing at the right angle
        if (hasHardware) {
            setTargetSpeed(TARGET_MOTOR_VELOCITY);
        }
        launching = true;
        reverse = false;
    }

    public void Reverse() {
        reverse = true;
        launching = false;
    }

    private void setWindmillPower(double power) {
        if (hasHardware) {
            windmill.setPower(-power);
        }
    }

    public void AutoLaunch() {
        // Spin the motors
        // TODO: make the motors spit the thing at the right angle
        if (hasHardware) {
            setTargetSpeed(1370);
        }
        launching = true;
        reverse = false;
    }

    public void IncreaseVelocity() {
        target += 100;
        setTargetSpeed(target);
    }

    public void DecreaseVelocity() {
        target -= 100;
        setTargetSpeed(target);
    }

    public boolean IsAtTargetVelocity() {
        return Math.abs(MOTOR_VELOCITY - TARGET_MOTOR_VELOCITY) < Target_windmill;
    }

    public void Stop() {
        if (hasHardware) {
            setTargetSpeed(0);
            setWindmillPower(0);
        }
        launching = false;
        reverse = false;
    }

    public void setTargetSpeed(double speed) {
        target = speed;
        //        top.setVelocity(speed);
        launcherPID.setTarget(speed);
    }

    public double getTargetSpeed() {
        return target;
    }

    private void setMotorPower(double pow) {
        if (launchMotor != null) {
            launchMotor.setPower(pow);
        }
    }

    public double getMotorSpeed() {
        if (launchMotor != null) {
            return -launchMotor.getVelocity();
        }
        return -1;
    }

    private final MovingStatistics smooth = new MovingStatistics(AMP_MOTOR_SAMPLE);

    public double getMotor1Current() {
        if (launchMotor == null) {
            return 0;
        }
        smooth.add(launchMotor.getAmperage(CurrentUnit.AMPS));
        return smooth.getMean();
    }

    @Override
    public void periodic() {
        double pidPower = launcherPID.update(getMotorSpeed());
        err = launcherPID.getLastError();
        MOTOR_VELOCITY = getMotorSpeed();
        power = launchMotor.getPower();
        setMotorPower(pidPower);
        // only launch if the wheel is spinning within 100 rpm of target
        if (launching && IsAtTargetVelocity()) {
            setWindmillPower(WINDMILL_SPIN);
        } else if (reverse) {
            setWindmillPower(-WINDMILL_SPIN);
        } else setWindmillPower(0.0);
    }
    // launcherP.f =  SPIN_F_SCALE * target + SPIN_VOLT_COMP * Math.min(PEAK_VOLTAGE, Hardware.voltage());
}
