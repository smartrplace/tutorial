#!/bin/bash

#Start the OGEMA-framework in a new screen
#
#Path to ogema-directory
ogemaPath="/home/pi/ogema"
#Run-directory to use
rundir="default"
#Name of screen
name="ogema"
echo "Starting OGEMA ..."
screen -dmS $name
screen -S $name -X stuff "cd $ogemaPath/\n"
screen -S $name -X stuff "./start.sh -uro\n"
echo "OGEMA started. Use 'screen -r $name' to attach to console"
