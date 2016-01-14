package Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import gui.Main;
import modules.VisionModule1;
import modules.VisionModuleSuite;

public class TestImages {

    Main app;
    Mat frame;
    VisionModule1 module;

    @Before
    public void setup() {
        app = new Main();
        module = new VisionModule1();
    }

    @Test
    public void test() {
        module.run(app, frame);
    }
}
