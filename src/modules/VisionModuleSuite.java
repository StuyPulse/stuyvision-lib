package modules;

import vision.ImageCaptureSource;
import vision.ModuleRunner;

public class VisionModuleSuite {

    /*
     * Add any mappings here from capture sources to vision modules
     * Available capture sources:
     *   - DeviceCaptureSource
     *   - VideoCaptureSource
     *   - ImageCaptureSource
     */
    static {
        String imagePath = VisionModuleSuite.class.getResource("").getPath() + "../../images/0.jpg";
        ModuleRunner.addMapping(new ImageCaptureSource(imagePath), new VisionModule1());
    }
}
