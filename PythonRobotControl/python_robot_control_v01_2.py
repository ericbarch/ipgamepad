#!/usr/bin/python -tt
#
# joystick-robot-control
#
# Created 07.11.2011 - Eric Barch (ttjcrew.com)
#
# Code adapted from:
# http://principialabs.com/joystick-control-of-a-servo/ (Brian D. Wendt)
#
# NOTE: This script requires the following Python modules:
# pygame   - http://www.pygame.org/
# Win32 users may also need:
# pywin32  - http://sourceforge.net/projects/pywin32/

import pygame
import threading
import socket

# Allow multiple joysticks
joy = []

UDP_IP="192.168.1.22" # IP Address of the Arduino Controller
UDP_PORT=4444 # Port we are sending data to

# Thread for outputting data to Arduino
class robotio(threading.Thread):
  def __init__(self):
    threading.Thread.__init__(self)
    self.event = threading.Event()

  def run(self):
    while True:
      datapacket=chr(y1Pos)+chr(x1Pos)+chr(y2Pos)+chr(x2Pos) # Create our four byte UDP packet
      sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) # Set up the UDP socket
      sock.sendto(datapacket, (UDP_IP, UDP_PORT)) # Fire the packet
      print "Y1: %d, X1: %d, Y2: %d, X2: %d" % (y1Pos, x1Pos, y2Pos, x2Pos) # Output joystick values on the screen
      self.event.wait(0.02) # 20ms delay - don't spam the controller

  def stop(self):
    self.event.set()

robotIOthread = robotio() # Define our theading object for robot IO
y1Pos = x1Pos = y2Pos = x2Pos = 127

# Handle joystick event (change)
def handleJoyEvent(e):
  if e.type == pygame.JOYAXISMOTION:
    if (e.dict['axis'] == 0):
      global x1Pos
      x1Pos = int(translateJoystick(e.dict['value'], -1, 1, 0, 255))
    elif (e.dict['axis'] == 1):
      global y1Pos
      y1Pos = int(translateJoystick(e.dict['value'], 1, -1, 0, 255))
    elif (e.dict['axis'] == 2):
      global x2Pos
      x2Pos = int(translateJoystick(e.dict['value'], -1, 1, 0, 255))
    elif (e.dict['axis'] == 3):
      global y2Pos
      y2Pos = int(translateJoystick(e.dict['value'], 1, -1, 0, 255))
  elif e.type == pygame.JOYBUTTONDOWN:
    if (e.dict['button'] == 0): # Button 0 - This is purely here as an example for button cases
      print "Button 0 detected.\n"

def translateJoystick(input, inMin, inMax, outMin, outMax):
  return (((input - inMin) * (outMax - outMin)) / ((inMax - inMin) + outMin))

# Wait for joystick input
def joystickControl():
  while True:
    e = pygame.event.wait()
    if (e.type == pygame.JOYAXISMOTION or e.type == pygame.JOYBUTTONDOWN):
      handleJoyEvent(e)

# Main method
def main():
  # Initialize pygame
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
    print "Joystick %d: " % (i) + joy[i].get_name() + "\n"
  global robotIOthread
  robotIOthread.start()

  # Run joystick listener loop
  joystickControl()

# Allow use as a module or standalone script
if __name__ == "__main__":
  main()
