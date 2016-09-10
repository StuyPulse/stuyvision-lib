Stuy Vision 2016
================

A library for interactive computer vision development, forked from a
[project](https://github.com/ChesleyTan/java-vision-gui) by two StuyPulse alumni.

## Installing OpenCV on your machine

If JAVA_HOME is not set, set it with

```
$ export JAVA_HOME=/path/to/jdk
```

This should be in your ~/.bashrc or .profile to persist across
reboots. (Do not include the dollar sign.)

To install OpenCV 3.0, first update your packages:

```
$ sudo apt-get update
$ sudo apt-get upgrade
```

, then install OpenCV's dependencies. Running `install-opencv-unix.sh`
will show you the dependencies, then ask you whether to continue. Say
no and install them.

Then run:

```
$ ./install-opencv-unix.sh
```

## Building with Ant
Make sure you have Apache Ant installed. You can check
by running `ant -version`.

Run

```
$ ant dist
```

to build the project and create `dist/stuyvision.jar`.

## Configuring camera settings

`setup-camera.sh` uses V4L (Video4Linux) to configure settings
like exposure and brightness (which should be installed
if you followed the OpenCV installation directions above).

## Setting up CV on a NVIDIA Jetson (Tegra)

To install OpenCV 3.0, run:

```
$ ./install-opencv-tegra.sh
```

Again, make sure to install the dependencies.

### Running your code on startup

To run your code automatically when the Tegra boots, you'll set up a
runlevel. Most Linux machines have 7 runlevels, numbered 0 through 6,
in which 0 shuts down the system, 6 reboots, and 1 through 5 startup
the machine in various different ways. You can read more about runlevels
[here](https://en.wikipedia.org/wiki/Runlevel) and
runlevels in Debian in particular [here](https://wiki.debian.org/RunLevel).

We will show setup of runlevel 4, a runlevel open for customization in Debian
and LSB (Linux Standard Base)-compliant distributions.

What will run in runlevel 4 is determined by the contents of `/etc/rc4.d/`.
`rc4.d` contains symbolic links to the scripts that will run when booting to
this run level.

Add a symbolic link to the script which will begin execution of your code.
Give it a name beginning with `S`, followed by a two-digit number,
and then a descriptive name. The two-digit number determines the order in
which the scripts will run.

E.g.:

```
cd /etc/rc4.d/
sudo ln -s /path/to/script S80run-cv
```

An example startup script can be seen [here](https://github.com/Team694/stuy-vision-2016/blob/master/run-cv.sh).

In order to set runlevel 4 as the default runlevel, open
`/etc/init/rc-sysinit.conf`

Find the line `env DEFAULT_RUNLEVEL=2`, and change it to

```
env DEFAULT_RUNLEVEL=4
```
