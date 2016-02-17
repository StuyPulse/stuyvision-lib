#!/bin/bash

# ANSI Escape Codes
RED="\033[1;31m"
GREEN="\033[1;32m"
YELLOW="\033[1;33m"
RESET="\033[m"

GUVCVIEW_PROFILE="/home/ubuntu/cv/stuy-vision-2016/guvcview.gpfl"
DEVICE="/dev/video0"

set -e # Abort if a command returns a non-zero exit code

if [[ ! $(pwd) =~ .*stuy-vision-2016 ]]; then
    printf "${RED}Please run from the root directory of stuy-vision-2016.${RESET}\n"
    exit 1
fi

if [[ ! -f /etc/NetworkManager/system-connections/Jetson ]]; then
    printf "${YELLOW}Copying over NetworkManager profile for the Jetson.${RESET}\n"
    sudo cp Jetson.nm.profile /etc/NetworkManager/system-connections/Jetson
fi

printf "${YELLOW}Sharing connection with the Jetson...${RESET}\n"
nmcli connection up id Jetson
printf "${GREEN}Done.${RESET}\n"

ssh -X ubuntu@10.42.0.12 "v4l2-ctl -c exposure_auto=1,exposure_absolute=5,brightness=30,contrast=10,saturation=200,white_balance_temperature_auto=0,sharpness=50; v4l2-ctl -c white_balance_temperature=4624"

printf "${GREEN}Successfully configured.${RESET}\n"
