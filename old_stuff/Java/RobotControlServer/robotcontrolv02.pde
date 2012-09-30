#include <Ethernet.h>
#include <Servo.h>
#include <string.h>
#include <stddef.h>
#include <stdlib.h>

//Constants
#define	NEUTRAL		90

//Global robot variables
int robotEnabled = 0;	 //0 - Robot Disabled, 1 - Robot Enabled
int robotAuton = 0;      //0 - Tele-op, 1 - Autonomous
unsigned long lastUpdate = 0;	//Keeps track of the last time (ms) we received data
int rxComplete = 0;	//Lets us know if we can process data

//Define joystick inputs
int left_ana = 0;
int right_ana = 0;
int left_ana_srv = 0;
int right_ana_srv = 0;
int btn_1 = 0;
int btn_2 = 0;
int btn_3 = 0;
int btn_4 = 0;
int btn_5 = 0;
int btn_6 = 0;
int btn_7 = 0;
int btn_8 = 0;
int btn_9 = 0;
int btn_10 = 0;
int btn_lab = 0;
int btn_rab = 0;
int btn_up = 0;
int btn_right = 0;
int btn_down = 0;
int btn_left = 0;

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

	//Only allow robot to be enabled if we've received data in the last second and robot is set to enabled
	if (robotEnabled && (millis() - lastUpdate) <= 1000)
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
				rxComplete = 1;
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
		left_ana_srv = map(left_ana, 0, 254, 0, 180); 
		right_ana = atoi(strtok_r(NULL,"|",&lastrxSplit));
		right_ana_srv = map(right_ana, 0, 254, 0, 180);
		btn_1 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_2 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_3 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_4 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_5 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_6 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_7 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_8 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_9 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_10 = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_lab = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_rab = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_up = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_right = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_down = atoi(strtok_r(NULL,"|",&lastrxSplit));
		btn_left = atoi(strtok_r(NULL,"|",&lastrxSplit));

		if (btn_7 && btn_8)
			robotEnabled = 1;
		else
			robotEnabled = 0;

		//Keep track of the last time we received data
		lastUpdate = millis();

		//All data grabbed, tx to the system so we can get more data (transmit current uptime, left/right PWM)
		client.print((millis()/1000)/60);
		client.print(",");
		client.print(left_ana);
		client.print(",");
		client.print(right_ana);
		client.print("\n");

		rxComplete = 0;
	}
}


void enabled()
{	
        leftDrive.write(left_ana_srv);
	rightDrive.write(right_ana_srv);
}


void disabled()
{
	leftDrive.write(NEUTRAL);
	rightDrive.write(NEUTRAL);
}
