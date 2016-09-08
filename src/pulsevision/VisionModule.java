package pulsevision;

import org.opencv.core.Mat;

import pulsevision.gui.VisionGui;

public abstract class VisionModule {

    public abstract void run(VisionGui app, Mat frame);

    public String getName() {
        return getClass().getSimpleName();
    }
}
