#!/bin/bash

v4l2-ctl -d /dev/video1 -c exposure_auto=1,exposure_absolute=5,brightness=30,contrast=10,saturation=200,white_balance_temperature_auto=0,sharpness=50
v4l2-ctl -d /dev/video1 -c white_balance_temperature=4624
