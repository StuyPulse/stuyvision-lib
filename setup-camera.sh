#!/bin/bash

# Get command line argument(s):
device=0
while getopts d:i option; do
    case $option in
        d) device=$OPTARG;;
        i)
            echo "Enter a video device to configure, or a number for /dev/videoN, of the following:"
            ls /dev/video*
            echo "Device:"
            read device;;
        \?) exit 1;;
    esac
done

v4l2-ctl -d $device -c exposure_auto=1,exposure_absolute=5,brightness=30,contrast=10,saturation=200,white_balance_temperature_auto=0,sharpness=50
v4l2-ctl -d $device -c white_balance_temperature=4624
