/*
 * RobotControl System v0.2 by Eric Barch
 * Socket based Joystick Data Feed
 */

package robotcontrol;

import net.java.games.input.*;
import java.net.*;
import java.io.*;

/**
 *
 * @author Eric Barch
 */
public class Main {

    //Get the USB controllers
    static Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

    //SET USB INPUT DEVICE HERE
    static Controller robotJoystick = controllers[10];

    //Print out all the available USB devices
    /*System.out.println("Robot joystick is: " + robotJoystick.getName());
    for (int j=0;j<controllers.length;j++) {
        //Get the components name
        System.out.println("Component "+j+": "+controllers[j].getName());
    }*/

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        Socket robotSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String rxData;
        String txData;

        try {
            robotSocket = new Socket("192.168.1.44", 4444);
            out = new PrintWriter(robotSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(robotSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for robot connection.");
            System.exit(1);
        }        

        //Signal the initial connection
        out.println("!");

        /* MAIN PROGRAM LOOP (Data tx/rx) */
        while((rxData = in.readLine()) != null) {
            System.out.println(rxData);

            //Assemble the txData variable we are transmitting to the robot
            txData = Integer.toString(grabJoystickData("leftPWM")) + "|" + Integer.toString(grabJoystickData("rightPWM"));
            txData += "|" + Integer.toString(grabJoystickData("B1"));
            txData += "|" + Integer.toString(grabJoystickData("B2"));
            txData += "|" + Integer.toString(grabJoystickData("B3"));
            txData += "|" + Integer.toString(grabJoystickData("B4"));
            txData += "|" + Integer.toString(grabJoystickData("B5"));
            txData += "|" + Integer.toString(grabJoystickData("B6"));
            txData += "|" + Integer.toString(grabJoystickData("B7"));
            txData += "|" + Integer.toString(grabJoystickData("B8"));
            txData += "|" + Integer.toString(grabJoystickData("B9"));
            txData += "|" + Integer.toString(grabJoystickData("B10"));
            txData += "|" + Integer.toString(grabJoystickData("LAB"));
            txData += "|" + Integer.toString(grabJoystickData("RAB"));
            txData += "|" + Integer.toString(grabJoystickData("Up"));
            txData += "|" + Integer.toString(grabJoystickData("Right"));
            txData += "|" + Integer.toString(grabJoystickData("Down"));
            txData += "|" + Integer.toString(grabJoystickData("Left"));
            txData += "#";

            //Kick it out to the robot
            out.println(txData);

            try {
                Thread.sleep(1); // Wait for 1ms
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        out.close();
        in.close();
        robotSocket.close();
    }

    static public int grabJoystickData(String axis) {

        //Grab new data from the controller
        robotJoystick.poll();

        //Grab individual components of controller input
        Component[] components = robotJoystick.getComponents();

        //Setup output variable
        int output = 0;

        //Button Feedback / Action Mapping
        if (axis.equals("B1") && components[5].getPollData() == 1)
            output = 1;
        else if (axis.equals("B2") && components[6].getPollData() == 1)
            output = 1;
        else if (axis.equals("B3") && components[7].getPollData() == 1)
            output = 1;
        else if (axis.equals("B4") && components[8].getPollData() == 1)
            output = 1;
        else if (axis.equals("B5") && components[9].getPollData() == 1)
            output = 1;
        else if (axis.equals("B6") && components[10].getPollData() == 1)
            output = 1;
        else if (axis.equals("B7") && components[11].getPollData() == 1)
            output = 1;
        else if (axis.equals("B8") && components[12].getPollData() == 1)
            output = 1;
        else if (axis.equals("B9") && components[13].getPollData() == 1)
            output = 1;
        else if (axis.equals("B10") && components[14].getPollData() == 1)
            output = 1;
        else if (axis.equals("LAB") && components[15].getPollData() == 1)
            output = 1;
        else if (axis.equals("RAB") && components[16].getPollData() == 1)
            output = 1;
        else if (axis.equals("Up") && components[4].getPollData() == .25)
            output = 1;
        else if (axis.equals("Right") && components[4].getPollData() == .50)
            output = 1;
        else if (axis.equals("Down") && components[4].getPollData() == .75)
            output = 1;
        else if (axis.equals("Left") && components[4].getPollData() == 1)
            output = 1;

        /* Dual Analog Joystick PWM Calculation */

        //Grab joystick values, multiply by -127, normalize by 127 to get PWM ranges
        Float fGrabY1 = ((components[2].getPollData() * -127) + 127);    //Left Joystick (y-axis)
        Float fGrabY2 = ((components[0].getPollData() * -127) + 127);    //Right Joystick (y-axis)

        //Convert floats to integers for PWM output
        int grabY1 = fGrabY1.intValue();
        int grabY2 = fGrabY2.intValue();

        //Output control data
        if (axis.equals("leftPWM"))
            output = grabY1;
        else if (axis.equals("rightPWM"))
            output = grabY2;

        return output;

        /*Grab analog joystick1 X/Y values
        Float fHeading = components[12].getPollData();          //Grab direction value (-1 to 1)
        Float fPower = (components[13].getPollData() * -127);   //Create power value from -127 to 127

        //Temporary Storage/Calculation Floats
        Float fLDrive;
        Float fRDrive;

        if (fHeading >= 0) {    //Turning Right
                fLDrive = fPower;
                fRDrive = fLDrive * (1 - fHeading);
        }
        else {                  //Turning Left
                fRDrive = fPower;
                fLDrive = fRDrive * (1 - (fHeading * -1));
        }

        //Normalize control signals to Neutral PWM
        fLDrive = fLDrive + 127;
        fRDrive = fRDrive + 127;

        //Convert Floats to Integer Values
        int LDrive = fLDrive.intValue();
        int RDrive = fRDrive.intValue();*/
    }

}