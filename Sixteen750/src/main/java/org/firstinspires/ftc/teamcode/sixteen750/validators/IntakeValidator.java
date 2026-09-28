package org.firstinspires.ftc.teamcode.sixteen750.validators;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.technototes.library.structure.ValidationOpMode;
import org.firstinspires.ftc.teamcode.sixteen750.Setup;

@Configurable
@TeleOp(name = "intake", group = "validators")
public class IntakeValidator extends ValidationOpMode {

    // theres 2 motors , each spinning one direction, or maybe same direction , depending where the motors is
    private DcMotor transferMotor1;
    private DcMotor transferMotor2;
    private DcMotor intakeMotor;

    @Override
    public void init() {
        super.init();
        intakeMotor = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.INTAKE_MOTOR);
        transferMotor1 = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.TRANSFER_MOTOR1);
        transferMotor2 = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.TRANSFER_MOTOR2);
    }

    @Override
    public void loop() {
        super.loop();
        addLine("Left stick x -> intake velocity");
        double a = (this.gamepad1.left_stick_x + 1) / 2;
        intakeMotor.setPower(a);
        addLine("Right stick x -> transfer1 velocity");
        double b = (this.gamepad1.right_stick_x + 1) / 2;
        transferMotor1.setPower(b);
        addLine("Right stick y -> transfer2 velocity");
        double c = (this.gamepad1.right_stick_y + 1) / 2;
        transferMotor2.setPower(c);
    }

    {
    }
}
