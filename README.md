IPGamepad
=========

**The goal of this project is to use an Android device as a controller/joystick to open hardware such as Arduinos or Netduinos. The main purpose for doing this is to create a simple and affordable robot control system that utilizes an off-the-shelf wifi router.**

![App Screenshot](http://i.imgur.com/fWPcB.png)

NOTE: The app currently requires you to have at least one finger on the joystick to send control data. This is a safety feature to allow the data flow to be easily interrupted. To change settings such as IP address, port, and packet transmit rate, tap menu and preferences.

The Arduino sketch listed under downloads is designed to run on Ethernet enabled Arduinos and take input from IPGamepad. The sketch is intended to be used for tank drive robots (only the Y axis used from the joysticks) and it outputs to 2 PWM channels (pins 5/6). This can be used to control speed controllers or even continuous turn servos if you wish. A Netduino can also be substituted in place of the Arduino and used with the provided code if desired.

By utilizing this with IPGamepad, your setup should look like this:

**Android Device**   ( ( ( 802.11x Wireless ) ) )   **Wireless Router** ---ETHERNET---**Arduino/Netduino**---PWM CABLE---**Motor Controllers**

To ensure everything works out of the box, make sure your network is setup on the 192.168.1.X scope. The Arduino will have a default IP of 192.168.1.22 and your Android device should have an IP somewhere in the scope. If you're using a Netduino, make sure to use the configuration tool to set the IP to 192.168.1.22. By default, digital pins 5/6 will be used for PWM output for both the Arduino/Netduino code.