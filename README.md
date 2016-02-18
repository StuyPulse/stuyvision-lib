Stuy Vision 2016
================

This is our CV project for FRC 2016: Stronghold.

Forked and made using an alumni's project, inspired by [CUAUV](https://github.com/cuauv)'s auv-vision-gui

## Setting Up

To install OpenCV 3.0, run:

```
export JAVA_HOME=/path/to/jvm
$ ./install-opencv-unix.sh
```

After OpenCV builds, update the Eclipse classpath:

```
$ cp .classpath-generic .classpath     # Generic JRE

OR

$ cp .classpath-osx .classpath         # OSX JRE-1.8 u65
```

## Setting Up on a NVIDIA Jetson

To install OpenCV 3.0, run:

```
export JAVA_HOME=/path/to/jvm
$ ./install-opencv-tegra.sh
```

To set up the runlevel and run the cv on boot, modify `/etc/init/rc-sysinit.conf` to use runlevel 4, which you will then modify

Find the line `env DEFAULT_RUNLEVEL=2`, and change it to
```
env DEFAULT_RUNLEVEL=4
```

What will run in runlevel 4 is determined by the contents of `/etc/rc4.d/`.
`rc4.d` contains symbolic links to the scripts that will run when booting to this run level.
Add a symbolic link to a script whose name starts with `S`, followed by a two-digit number,
and then a descriptive name. The two-digit number determines the order in which the scripts will run.

```
cd /etc/rc4.d/
sudo ln -s /path/to/script S80run-cv
```
