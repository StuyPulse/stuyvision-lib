package modules;

import java.io.File;

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
        File directory = new File(imageDirectory);
        File[] directoryListing = directory.listFiles();
        for (int i = 0; i < directoryListing.length && i < 2; i++) {
            ModuleRunner.addMapping(new ImageCaptureSource(imageDirectory + directoryListing[i].getName()), new VisionModule1());
        }
    }
}
