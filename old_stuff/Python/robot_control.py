#!/usr/bin/env python
# 
# arduino-usb-joystick-control
#  
# created 08.14.2010 - Eric Barch (ttjcrew.com) 
#   
# code adapted from:  
# http://principialabs.com/joystick-control-of-a-servo/ (Brian D. Wendt)
#  
# NOTE: This script requires the following Python modules:
# pygame   - http://www.pygame.org/  
# Win32 users may also need:  
# pywin32  - http://sourceforge.net/projects/pywin32/  

import pygame
import threading
import socket

# allow multiple joysticks
joy = []

UDP_IP="192.168.1.33"
UDP_PORT=4444

# handle output to robot
class robotio(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.event = threading.Event()

    def run(self):
        while not self.event.is_set():
	     MESSAGE=chr(xPos)+chr(yPos)
	     sock = socket.socket( socket.AF_INET, socket.SOCK_DGRAM ) # UDP
  	     sock.sendto(MESSAGE, (UDP_IP, UDP_PORT))
             print "X: %d, Y: %d" % (xPos, yPos)
             self.event.wait(0.01) # 10ms

    def stop(self):
        self.event.set()

tmr = robotio() 
xPos = 90
yPos = 90

# handle joystick event  
def handleJoyEvent(e):  
     if e.type == pygame.JOYAXISMOTION:
         axis = "unknown"
         if (e.dict['axis'] == 1):
             axis = "X"
   
         if (e.dict['axis'] == 3):  
             axis = "Y"
   
         if (axis != "unknown"):
             # Arduino joystick-servo hack  
             if (axis == "X"):
		pos = e.dict['value']
                # convert joystick position to servo increment, 0-180  
                move = round(pos * 90, 0)
		global xPos
                if (move < 0):  
                    xPos = int(90 - abs(move))  
                else:  
                    xPos = int(move + 90)
		xPos = 180 - xPos
	     if (axis == "Y"):
		pos = e.dict['value']  
                # convert joystick position to servo increment, 0-180  
                move = round(pos * 90, 0)
		global yPos
                if (move < 0):  
                    yPos = int(90 - abs(move))  
                else:  
                    yPos = int(move + 90)
		yPos = 180 - yPos
   
     #elif e.type == pygame.JOYBUTTONDOWN:
         # Button 0 (trigger) to quit  
         #if (e.dict['button'] == 0):  
             #print "Quit.\n"
	     #global tmr
	     #tmr.stop()
             #quit()  
     else:  
         pass
  
# wait for joystick input  
def joystickControl():
    while True:  
        e = pygame.event.wait()
        if (e.type == pygame.JOYAXISMOTION or e.type == pygame.JOYBUTTONDOWN):  
            handleJoyEvent(e)
  
# main method  
def main():
    # initialize pygame
    pygame.joystick.init()  
    pygame.display.init()  
    if not pygame.joystick.get_count():  
        print "\nPlease connect a joystick and run again.\n"  
        quit()  
    print "\n%d joystick(s) detected." % pygame.joystick.get_count()  
    for i in range(pygame.joystick.get_count()):  
        myjoy = pygame.joystick.Joystick(i)  
        myjoy.init()  
        joy.append(myjoy)  
        print "Joystick %d: " % (i) + joy[i].get_name()  
    #print "Depress button 1 to quit.\n"
    global tmr
    tmr.start()
   
    # run joystick listener loop  
    joystickControl()  
  
# allow use as a module or standalone script  
if __name__ == "__main__":  
    main()
