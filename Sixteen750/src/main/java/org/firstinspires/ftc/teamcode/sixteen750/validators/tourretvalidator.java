package org.firstinspires.ftc.teamcode.sixteen750.validators;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.technototes.library.hardware.motor.EncodedMotor;
import com.technototes.library.hardware.servo.Servo;
import com.technototes.library.structure.CommandOpMode;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
@TeleOp(name = "turret", group = "validators")
public class tourretvalidator extends CommandOpMode {

    private Servo rotatingservo1;
    private Servo rotatingservo2;

    private Servo hoodservo;

    private EncodedMotor<DcMotorEx> launcher;
    private DcMotor intake, transfer1, transfer2;
    public static double rotateleft = 0.15;
    public static double rotateright = 0.85;

    public static double up = 0.1;
    public static double down = 0.5;

    public static double velocity = -0.5;
    public static double velocity2 = 0.5;

    @Override
    public void uponInit() {
        rotatingservo2 = new Servo(Setup.HardwareNames.TURRET_SERVO2).expandedRange();
        rotatingservo1 = new Servo(Setup.HardwareNames.TURRET_SERVO1).expandedRange();
        hoodservo = new Servo(Setup.HardwareNames.HOOD_SERVO);
        launcher = new EncodedMotor<>(Setup.HardwareNames.LAUNCHER_MOTOR);
        intake = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.INTAKE_MOTOR);
        transfer1 = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.TRANSFER_MOTOR1);
        transfer2 = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.TRANSFER_MOTOR2);

    }

    @Override
    public void runLoop() {
        super.runLoop();

        telemetry.addLine("Move right stick to rotate turret");
        double pos = (this.gamepad1.right_stick_x + 1.25) / 2.5;
        rotatingservo2.setPosition(pos);
        rotatingservo1.setPosition(pos);

        telemetry.addLine("Move up and down");
        double hoodpos = (this.gamepad1.left_stick_y + 1) / 2;
        hoodservo.setPosition(hoodpos);
        telemetry.addData("hoodpos", hoodpos);

        telemetry.addLine("Increase velocity");
        double po = ((this.gamepad1.left_stick_x + 1) / 2) *-1;
        launcher.setPower(-.8);
        telemetry.addData("launcher speed", launcher.getPower());
        double velo = (this.gamepad1.right_trigger);
        intake.setPower(velo);
        transfer1.setPower(velo);
        transfer2.setPower(velo);

    }
}
