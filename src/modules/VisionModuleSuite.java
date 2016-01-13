package modules;

import vision.ImageCaptureSource;
import vision.ModuleRunner;

public class VisionModuleSuite {

    /**
     * Add any mappings here from capture sources to vision modules
     * Available capture sources:
     *   - DeviceCaptureSource
     *   - VideoCaptureSource
     *   - ImageCaptureSource
     */
    static {
        String imageDirectory = VisionModuleSuite.class.getResource("").getPath() + "../../images/";
        ModuleRunner.addMapping(new ImageCaptureSource(imageDirectory + "0.jpg"), new VisionModule1());
    }
}
