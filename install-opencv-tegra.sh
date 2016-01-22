#/bin/bash

OPENCV_VERSION=3.0.0

echo "Installing opencv-$OPENCV_VERSION for Nvidia Jetson TK1 on Tegra Processor ...\n"
echo "================================================================================"
sudo apt-get -y install libopencv-dev build-essential cmake git libgtk2.0-dev pkg-config python-dev python-numpy libdc1394-22 libdc1394-22-dev libjpeg-dev libpng12-dev libtiff4-dev libjasper-dev libavcodec-dev libavformat-dev libswscale-dev libxine-dev libgstreamer0.10-dev libgstreamer-plugins-base0.10-dev libv4l-dev libtbb-dev libqt4-dev libfaac-dev libmp3lame-dev libopencore-amrnb-dev libopencore-amrwb-dev libtheora-dev libvorbis-dev libxvidcore-dev x264 v4l-utils unzip
sudo apt-get install libavformat-dev libavutil-dev libswscale-dev make cmake-curses-gui g++ libv4l-dev libeigen3-dev libglew1.6-dev libgtk2.0-dev

cd lib
echo "Downloading opencv-$OPENCV_VERSION"
wget -c https://github.com/Itseez/opencv/archive/$OPENCV_VERSION.zip
unzip $OPENCV_VERSION.zip
rm -f $OPENCV_VERSION.zip

echo "Building opencv-$OPENCV_VERSION"
cd opencv-$OPENCV_VERSION
mkdir -p build
cd build
cmake -D BUILD_SHARED_LIBS=OFF -D WITH_CUDA=ON -D CUDA_ARCH_BIN="3.2" -D CUDA_ARCH_PTX="" -D BUILD_TESTS=OFF -D BUILD_PERF_TESTS=OFF -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -D WITH_TBB=OFF -D WITH_V4L=ON -D WITH_QT=ON -D WITH_OPENGL=OFF ..
make -j4

if [[ $? = 0 ]]; then
    echo "Finished building opencv-$OPENCV_VERSION"
    echo "Import opencv.userlibraries to your Eclipse user libraries to use Java bindings for opencv"
else
    echo "There were errors building opencv-$OPENCV_VERSION :("
    echo "Try googling the errors produced above for solutions."
fi
