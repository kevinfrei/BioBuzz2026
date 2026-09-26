package org.firstinspires.ftc.sixteen750.validators;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.library.hardware.servo.Servo;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.structure.ValidationOpMode;
import org.firstinspires.ftc.sixteen750.Setup;

@Configurable
@TeleOp(name = "tourret", group = "validators")
public class tourretvalidator extends ValidationOpMode {

    private Servo rotatingservo1;
    private Servo rotatingservo2;

    public static double rotateleft = 0.5;
    public static double rotateright = 0.5;

    @Override
    public void init() {
        super.init();
        rotatingservo2 = this.hardwareMap.get(Servo.class, Setup.HardwareNames.ROTATINGSERVO2);
        rotatingservo1 = this.hardwareMap.get(Servo.class, Setup.HardwareNames.ROTATINGSERVO1);
    }

    @Override
    public void loop() {
        super.loop();
        addLine("Press left bump to rotate");
        if (this.gamepad1.left_bumper) {
            rotatingservo1.setPosition(rotateleft);
        }
        addLine("Press right bump to rotate");
        if (this.gamepad1.right_bumper) {
            rotatingservo2.setPosition(rotateright);
        }
    }
}
