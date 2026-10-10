package org.firstinspires.ftc.teamcode.twenty403.validation;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.technototes.library.structure.ValidationOpMode;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
@TeleOp(name = "Intake", group = "validation")
public class IntakeValidator extends ValidationOpMode {

    private DcMotorEx intakeMotor;
    private CRServo left;
    private CRServo right;
    public static double intakeVelocity = 2000;
    public static double intakeVelocity2 = -200;
    public static double spinLeftFlower = 1;
    public static double spinRightFlower = 1;

    @Override
    public void init() {
        super.init();
        intakeMotor = this.hardwareMap.get(DcMotorEx.class, Setup.HardwareNames.INTAKEMOTOR);
        left = this.hardwareMap.get(CRServo.class, Setup.HardwareNames.LEFTINTAKESERVO);
        right = this.hardwareMap.get(CRServo.class, Setup.HardwareNames.RIGHTINTAKESERVO);
    }

    @Override
    public void loop() {
        super.loop();
        addLine("Press up on d-pad to intake");
        addLine("Press down on d-pad to spit out");
        if (this.gamepad1.dpad_up) {
            intakeMotor.setVelocity(intakeVelocity);
        } else if (this.gamepad1.dpad_down) {
            intakeMotor.setVelocity(intakeVelocity2);
        } else {
            intakeMotor.setVelocity(0);
        }

        addLine("Press left bumper to intake left side (positive)");
        addLine("Press left trigger to intake left side (negative)");
        if (this.gamepad1.left_bumper) {
            left.setPower(spinLeftFlower);
        } else if (this.gamepad1.left_trigger_pressed) {
            left.setPower(-spinLeftFlower);
        } else {
            left.setPower(0);
        }

        addLine("Press right bumper to intake right side (positive)");
        addLine("Press right trigger to intake right side (negative)");
        if (this.gamepad1.right_bumper) {
            right.setPower(spinRightFlower);
        } else if (this.gamepad1.right_trigger_pressed) {
            right.setPower(-spinRightFlower);
        } else {
            right.setPower(0);
        }
    }
}
