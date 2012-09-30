/************************************
  Arduino Based Robot Control System v0.1
  by Eric Barch (ttjcrew.com)
************************************/
#include <SPI.h>
#include <Ethernet.h>
#include <EthernetUdp.h>
#include <Servo.h>

//Set the MAC address, static IP, gateway, and subnet of the network
byte mac[] = { 0x00, 0x1D, 0x60, 0xAF, 0x03, 0x35 };
byte ip[]  = { 192, 168, 1, 22 };

//UDP Stuff
const int PORT = 4444;            //Port of incoming UDP data

const int PACKET_SIZE = 8;        //Size of the joystick data packet
byte joystick_data[PACKET_SIZE];  //Byte array for incoming data - [0] = leftY, [1] = leftX, [2] = rightY, [3] = rightX

//Robot specific stuff
boolean lastState = false;           //Keeps track of when we go between enabled/disabled or vice versa   
unsigned long lastUpdate = 0;        //Keeps track of the last time (ms) we received data

// An EthernetUDP instance to let us send and receive packets over UDP
EthernetUDP Udp;


//Define robot outputs
int pwm01 = 5;  //Digital Pin 5
int pwm02 = 6;  //Digital Pin 6

//Speed Controller/Servo Objects
Servo leftDrive;
Servo rightDrive;

void setup() {
  Serial.begin(9600);  //Setup serial comms for debugging
  
  // Start Ethernet and UDP
  Ethernet.begin(mac,ip);
  Udp.begin(PORT);
  
  Serial.println("Robot control system initialized.");
}


void loop() {
    xferdata();
  
    //Only allow robot to be enabled if we've received data in the last 100ms and robot is set to enabled
    if (((millis() - lastUpdate) <= 100) && (millis() > 500))  //Robot is disabled for first 500ms of runtime
      enabled();
    else
      disabled();
}


/* This function's sole purpose is to receive data and shove it into the joystick_data byte array */
void xferdata()
{
  int packetSize = Udp.parsePacket();
  if(packetSize)
  {
    Udp.read(joystick_data, PACKET_SIZE);
    lastUpdate = millis();
  }
}


void enabled()
{
    //If we were last disabled, we need to attach the PWM outputs
    if (lastState == false) {
      leftDrive.attach(pwm01);
      rightDrive.attach(pwm02);
    }
  
    //Output the left/right drive PWMs based on joystick input (0-180)
    leftDrive.write(map((long)joystick_data[0], 0, 255, 0, 180));
    rightDrive.write(map((long)joystick_data[2], 0, 255, 0, 180));
    
    //We are enabled
    lastState = true;
}


void disabled()
{
    //Robot is disabled, detach PWM outputs
    leftDrive.detach();
    rightDrive.detach();
    
    //We are disabled
    lastState = false;
}
