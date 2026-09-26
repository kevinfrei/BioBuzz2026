package org.firstinspires.ftc.sixteen750.validators;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.technototes.library.structure.ValidationOpMode;
import org.firstinspires.ftc.sixteen750.Setup;

@Configurable
@TeleOp
public class IntakeValidator extends ValidationOpMode {

    // theres 2 motors , each spinning one direction, or maybe same direction , depending where the motors is
    private DcMotor transferMotor1;
    private DcMotor transferMotor2;

    @Override
    public void init() {
        super.init();
        transferMotor1 = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.TRANSFERMOTOR1);
        transferMotor2 = this.hardwareMap.get(DcMotor.class, Setup.HardwareNames.TRANSFERMOTOR2);
    }

    @Override
    public void loop() {
        super.loop();
        addLine("Press right bumper to intake");
    }

    {
    }
}
