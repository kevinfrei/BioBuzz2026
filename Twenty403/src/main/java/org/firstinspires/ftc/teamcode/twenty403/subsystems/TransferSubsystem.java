package org.firstinspires.ftc.teamcode.twenty403.subsystems;

import com.bylazar.configurables.annotations.Configurable;
import com.technototes.library.hardware.motor.CRServo;
import com.technototes.library.subsystem.Subsystem;
import org.firstinspires.ftc.teamcode.twenty403.Hardware;
import org.firstinspires.ftc.teamcode.twenty403.Setup;

@Configurable
public class TransferSubsystem implements Subsystem {

    public static double TRANSFER_SPEED = 1;
    public static double REJECT_SPEED = -0.5;

    public TransferSubsystem(Hardware h) {
        if (Setup.Connected.TRANSFER) {
            _spinner = h.transferServo;
        }
    }

    public void Transfer() {
        setServo(TRANSFER_SPEED);
    }

    public void Stop() {
        setServo(0);
    }

    public void Reject() {
        setServo(REJECT_SPEED);
    }

    // Hardware Interface
    CRServo _spinner;

    private void setServo(double power) {
        if (_spinner != null) {
            _spinner.setPower(power);
        }
    }
}
