package org.firstinspires.ftc.teamcode.twenty403.validation;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.technototes.library.structure.ValidationOpMode;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
@TeleOp(name = "Launch", group = "validation")
public class LaunchValidator extends ValidationOpMode {

    private DcMotorEx launchmotor;
    private CRServo spinnything;
    public static double launchvelocity = 2000;
    public static double intakeVelocity2 = -200;
    public static double spinspeed = -1;
    public static double spinRightFlower = 1;

    @Override
    public void init() {
        super.init();
        launchmotor = this.hardwareMap.get(DcMotorEx.class, Setup.HardwareNames.LAUNCHER);
        spinnything = this.hardwareMap.get(CRServo.class, Setup.HardwareNames.SPIN);
        launchmotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    @Override
    public void loop() {
        super.loop();
        addLine("Press up on d-pad to launch");
        if (this.gamepad1.dpad_up) {
            launchmotor.setVelocity(launchvelocity);
        } else {
            launchmotor.setVelocity(0);
        }

        addLine("Press right bumper to spin");
        if (this.gamepad1.right_bumper) {
            spinnything.setPower(spinspeed);
        } else {
            spinnything.setPower(0);
        }
    }
}
