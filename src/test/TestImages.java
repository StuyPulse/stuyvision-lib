package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import gui.Main;
import modules.StuyVisionModule;
import modules.VisionModuleSuite;

public class TestImages {

    Main app;
    Mat frame;
    StuyVisionModule module;

    @Before
    public void setup() {
        app = new Main();
        module = new StuyVisionModule();
    }

    @Test
    public void test() {
        module.run(app, frame);
    }
}
