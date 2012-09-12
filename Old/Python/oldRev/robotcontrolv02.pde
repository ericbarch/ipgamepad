#include <Ethernet.h>
#include <Servo.h>
#include <string.h>
#include <stddef.h>
#include <stdlib.h>

//Constants
#define	NEUTRAL		90

boolean robotEnabled = false;    //Robot Enable/Disable Killswitch
boolean rxComplete = false;      //Lets us know if we can process data
double lastUpdate = 0;  //Keeps track of the last time (ms) we received data

//Define joystick inputs
int left_ana = 0;
int right_ana = 0;
int btn_7 = 0;
int btn_8 = 0;

//Define robot outputs
int pwm01 = 5;
int pwm02 = 6;

//Speed Controller Objects
Servo leftDrive;
Servo rightDrive;

//Networking stuff
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
byte ip[] = { 192, 168, 1, 44 };

Server server(4444);


void setup()
{
	Ethernet.begin(mac, ip);
	server.begin();
  	Serial.begin(9600);
  
        leftDrive.attach(pwm01);
	rightDrive.attach(pwm02);

  	Serial.println("Robot booted.");
}


void loop()
{
  	xferdata();

	//Only allow robot to be enabled if we've received data in the last quarter second and robot is set to enabled
	if (robotEnabled && (millis() - lastUpdate) <= 250)
		enabled();
	else
		disabled();
}


void xferdata()
{
	Client client = server.available();
	char rxData[100];	//Our data received from our controller is loaded in here
	char *lastrxSplit;	//Used to keep track of where we are in the string splitting process
	int rxDatalength = 0;	//Keeps track of the index of rxData

	if (client) {
		while (client.connected() && client.available()) {
			char c = client.read();

			if (c == '#') {
				rxComplete = true;
				break;
			}
			
			if (c != '!')
				rxData[rxDatalength++] = c;
			else
				client.println("Connection initialized.");
		}
	}
	
	if (rxComplete)	//Process data
	{
		left_ana = atoi(strtok_r(rxData,"|",&lastrxSplit));
                right_ana = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_7 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_8 = atoi(strtok_r(NULL,"|",&lastrxSplit));

		if (btn_7 && btn_8)
			robotEnabled = true;
		else
			robotEnabled = false;

		//Keep track of the last time we received data
		lastUpdate = millis();

		//All data grabbed, tx to the system so we can get more data (transmit current uptime, left/right PWM)
		client.print((millis()/1000)/60);
		client.print(",");
		client.print(left_ana);
		client.print(",");
		client.print(right_ana);
		client.print("\n");

		rxComplete = false;
	}
}


void enabled()
{	
        leftDrive.write(map(left_ana, -100, 100, 0, 180));
	rightDrive.write(map(right_ana, -100, 100, 0, 180));
}


void disabled()
{
	leftDrive.write(NEUTRAL);
	rightDrive.write(NEUTRAL);
}
