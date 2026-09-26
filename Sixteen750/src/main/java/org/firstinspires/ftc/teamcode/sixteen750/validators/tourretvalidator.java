package org.firstinspires.ftc.teamcode.sixteen750.validators;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
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
    public static double rotateleft = 0.1;
    public static double rotateright = 0.9;

    public static double up = 0;
    public static double down = 0.5;

    public static double velocity = -0.5;
    public static double velocity2 = 0.5;

    @Override
    public void uponInit() {
        rotatingservo2 = new Servo(Setup.HardwareNames.ROTATINGSERVO2).expandedRange();
        rotatingservo1 = new Servo(Setup.HardwareNames.ROTATINGSERVO1).expandedRange();
        hoodservo = new Servo(Setup.HardwareNames.HOOD_SERVO);
        launcher = new EncodedMotor<>(Setup.HardwareNames.LAUNCHER_MOTOR);
    }

    @Override
    public void runLoop() {
        super.runLoop();

        telemetry.addLine("Move right stick to rotate turret");
        double pos = (this.gamepad1.right_stick_x + 1) / 2;
        rotatingservo2.setPosition(pos);
        rotatingservo1.setPosition(pos);

        telemetry.addLine("Move up and down");
        double hoodpos = (this.gamepad1.left_stick_y + 1) / 2;
        hoodservo.setPosition(hoodpos);
        telemetry.addData("hoodpos", hoodpos);

        telemetry.addLine("Increase velocity");
        double po = (this.gamepad1.left_stick_x + 1) / 2;
        launcher.setPower(po);
        telemetry.addData("launcher speed", launcher.getPower());
    }
}
