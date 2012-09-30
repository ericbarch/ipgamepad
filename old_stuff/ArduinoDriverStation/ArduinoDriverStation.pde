#include <SPI.h>
#include <Ethernet.h>
#include <Udp.h>
#include <PS2X_lib.h>

PS2X ps2x; // create PS2 Controller Class

byte mac[] = { 0x00, 0x1D, 0x60, 0xAF, 0x03, 0x31 };
byte ip[]  = { 192, 168, 1, 33 };

//UDP Stuff
byte ROBOT_IP[] = { 192, 168, 1, 22 };    //IP Address of Robot
unsigned int ROBOT_PORT = 4444;           //Port of outgoing UDP data
const int PACKET_SIZE= 5;                 // Size of the data packet
byte joystick[PACKET_SIZE];               //buffer to hold incoming and outgoing packets 

void setup() {
  ps2x.config_gamepad(7,5,4,6,true,true);  //setup GamePad(clock, command, attention, data) pins + rumble/pressures
  
  // Start Ethernet and UDP
  Ethernet.begin(mac,ip);
  Udp.begin(ROBOT_PORT);
  
  Serial.begin(9600);
  Serial.println("Robot Driver Station v0.1 Initialized");
}

void loop() {
  ps2x.read_gamepad(false, 0);        //read controller
  //Build the joystick byte array
  joystick[0] = 255 - ps2x.Analog(PSS_LY);
  joystick[1] = ps2x.Analog(PSS_LX);
  joystick[2] = 255 - ps2x.Analog(PSS_RY);
  joystick[3] = ps2x.Analog(PSS_RX);
  if (ps2x.Button(PSB_R2))
    joystick[4] = 255;
  else
    joystick[4] = 0;
  Udp.sendPacket(joystick, PACKET_SIZE, ROBOT_IP, ROBOT_PORT);
  delay(20);
}
