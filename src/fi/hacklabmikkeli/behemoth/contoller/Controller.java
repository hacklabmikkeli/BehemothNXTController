package fi.hacklabmikkeli.behemoth.contoller;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.util.Delay;

public class Controller {

  public static final String REMOTE_DEVICE = "LINKKI6";
  
  public static void main(String[] args) {

    RemoteDevice behemoth = Bluetooth.getKnownDevice(REMOTE_DEVICE);
    int currentLeftValue = 0;
    int currentRightValue = 0;
    boolean escBtnWasPressed = false;
    boolean enterBtnWasPressed = false;
    boolean modeBtnWasPressed = false;
    boolean leftBtnWasPressed = false;
    boolean rightBtnWasPressed = false;

    if (behemoth == null) {
      System.out.println("Behemoth not found");
      Button.waitForAnyPress();
      System.exit(1);
    }

    BTConnection btc = Bluetooth.connect(behemoth);

    if (btc == null) {
      System.out.println("Could not connect to behemoth");
      Button.waitForAnyPress();
      System.exit(1);
    }
    
    DataOutputStream behemothOutputStream = btc.openDataOutputStream();

    NXTRegulatedMotor rightJoystick = Motor.C;
    NXTRegulatedMotor leftJoystick = Motor.A;
    TouchSensor modeBtn = new TouchSensor(SensorPort.S4);

    rightJoystick.resetTachoCount();
    leftJoystick.resetTachoCount();
    rightJoystick.flt();
    leftJoystick.flt();

    while (true) {
      int rightMotorSpeed = normalizeTachoCount(rightJoystick.getTachoCount());
      if (rightMotorSpeed != currentRightValue) {
        writeToStream(behemothOutputStream, 0, rightMotorSpeed);
        Delay.msDelay(1);
      }
      currentRightValue = rightMotorSpeed;
      
      int leftMotorSpeed = normalizeTachoCount(leftJoystick.getTachoCount());
      if (leftMotorSpeed != currentLeftValue) {
        writeToStream(behemothOutputStream, 1000, leftMotorSpeed);
      }
      currentLeftValue = leftMotorSpeed;
      
      boolean enterBtnPressed = Button.ENTER.isDown();
      if (enterBtnPressed && !enterBtnWasPressed) {
        writeToStream(behemothOutputStream, 2000, 750);
      }
      
      enterBtnWasPressed = enterBtnPressed;
      
      boolean escBtnPressed = Button.ESCAPE.isDown();
      if (escBtnPressed && !escBtnWasPressed) {
        writeToStream(behemothOutputStream, 2000, 250);
      }
      
      escBtnWasPressed = escBtnPressed;
      
      boolean modeBtnPressed = modeBtn.isPressed();
      if (modeBtnPressed && !modeBtnWasPressed) {
        writeToStream(behemothOutputStream, 3000, 250);
      }
      
      modeBtnWasPressed = modeBtnPressed;
      
      boolean leftBtnPressed = Button.LEFT.isDown();
      if (leftBtnPressed && !leftBtnWasPressed) {
        writeToStream(behemothOutputStream, 4000, 250);
      }
      
      leftBtnWasPressed = leftBtnPressed;
      
      boolean rightBtnPressed = Button.RIGHT.isDown();
      if (rightBtnPressed && !rightBtnWasPressed) {
        writeToStream(behemothOutputStream, 5000, 250);
      }
      
      rightBtnWasPressed = rightBtnPressed;
      
    }    
  }
  
  private static void writeToStream(DataOutputStream stream, int baseValue, int value) {
    try {
      stream.writeInt(baseValue + value);
      stream.flush();
    } catch (IOException e) {
      System.out.println("Error writing to stream");
    }
  }
  
  private static int normalizeTachoCount(int tachoCount) {
    int result = -tachoCount;
    if (result > 80) {
      result =  80;
    } else if (result < -100) {
      result = -100;
    }
    
    return map(result, -100, 80, 1, 1000);
  }


  private static int map(int value, int fromRangeLow, int fromRangeHigh, int toRangeLow, int toRangeHigh) {
    return (value - fromRangeLow) * (toRangeHigh - toRangeLow) / (fromRangeHigh - fromRangeLow) + toRangeLow;
  }
  
}
