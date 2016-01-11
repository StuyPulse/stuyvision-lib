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
        ModuleRunner.addMapping(new ImageCaptureSource(
                "/Users/Danny/Downloads/RealFullField/3.jpg"), new VisionModule1());
    }
}
