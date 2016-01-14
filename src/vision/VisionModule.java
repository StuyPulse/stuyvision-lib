package vision;

import org.opencv.core.Mat;

import gui.Main;

public abstract class VisionModule {

    public abstract Object run(Main app, Mat frame);

    public String getName() {
        return getClass().getSimpleName();
    }
}
