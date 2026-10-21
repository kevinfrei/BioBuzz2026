package org.firstinspires.ftc.teamcode.twenty403.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.logger.Log;
import com.technototes.library.logger.Loggable;
import com.technototes.library.subsystem.Subsystem;
import com.technototes.library.util.PIDFController;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class LauncherSubsystem implements Loggable, Subsystem {

    public static double TARGET_MOTOR_VELOCITY = 1350; //.58; // 0.5 // /1.0
    public static double WINDMILL_SPIN = -0.15;
    public static double REVERSE_WINDMILL = 0.15;
    public static double Target_windmill = 100;

    boolean hasHardware;
    public static EncodedMotor<DcMotorEx> launchMotor;
    public CRServo windmill;
    // this doesn't really matter as much since f calculation is just overriding it :(
    public static PIDFCoefficients launcherP = new PIDFCoefficients(0.006, 0.0, 0.0, 0);
    public static double SPIN_F_SCALE = 0.00016;
    public static double SPIN_VOLT_COMP = 0.0216;
    public static double DIFFERENCE = 0.0046;
    public static double PEAK_VOLTAGE = 13;
    public static boolean ISLAUNCHMOTORREVERSED = true;
    private static PIDFController launcherPID;
    private double voltage;

    @Log(name = "Launcher Velo: ")
    public static double MOTOR_VELOCITY;

    @Log(name = "Target Speed: ")
    public static double target;

    @Log(name = "Launcher Power: ")
    public static double power;

    @Log(name = "Error")
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
            launcherPID = new PIDFController(launcherP, target ->
                target == 0
                    ? 0
                    : SPIN_F_SCALE * target + SPIN_VOLT_COMP * Math.min(PEAK_VOLTAGE, h.voltage())
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
        if (hasHardware && launching) {
            target += 100;
            setTargetSpeed(target);
        }
    }

    public void DecreaseVelocity() {
        if (hasHardware && launching) {
            target -= 100;
            setTargetSpeed(target);
        }
    }

    public boolean GetCurrentTargetVelocity() {
        return launchMotor.getVelocity() >= 1300;
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
        double power = Math.clamp(pow, -0.53, 0.53);
        if (launchMotor != null) {
            launchMotor.setPower(power);
        }
    }

    public double getMotorSpeed() {
        if (launchMotor != null) {
            return launchMotor.getVelocity();
        }
        return -1;
    }

    @Override
    public void periodic() {
        setMotorPower(launcherPID.update(getMotorSpeed()));
        err = launcherPID.getLastError();
        MOTOR_VELOCITY = getMotorSpeed();
        power = launchMotor.getPower();
        // only launch if the wheel is spinning within 100 rpm of target
        if (launching && Math.abs(MOTOR_VELOCITY - TARGET_MOTOR_VELOCITY) < Target_windmill) {
            setWindmillPower(WINDMILL_SPIN);
        } else if (reverse) {
            setWindmillPower(-WINDMILL_SPIN);
        } else setWindmillPower(0.0);
    }
    // launcherP.f =  SPIN_F_SCALE * target + SPIN_VOLT_COMP * Math.min(PEAK_VOLTAGE, Hardware.voltage());
}
