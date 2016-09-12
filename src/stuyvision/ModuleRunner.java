package stuyvision;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import stuyvision.VisionModule;
import stuyvision.capture.CaptureSource;
import stuyvision.capture.Loopable;
import stuyvision.util.DebugPrinter;

public class ModuleRunner {
    private static ArrayList<CaptureSourceToVisionModuleMapper> sourceDestMap = new ArrayList<CaptureSourceToVisionModuleMapper>();
    private static final int FPS = 10;

    static {
        DebugPrinter.println("Loading OpenCV version " + Core.VERSION);
        DebugPrinter.println("Native library path: " + System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        DebugPrinter.println("System.loadLibrary succeeded");
    }

    public void addMapping(CaptureSource captureSource, VisionModule... modules) {
        sourceDestMap.add(new CaptureSourceToVisionModuleMapper(captureSource, modules));
    }

    public void run() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (CaptureSourceToVisionModuleMapper captureSourceMap : sourceDestMap) {
                        if (captureSourceMap.captureSource.isOpened()) {
                            Mat frame = new Mat();
                            boolean success = captureSourceMap.captureSource.readSized(frame, frame);
                            if (!success) {
                                if (captureSourceMap.captureSource instanceof Loopable) {
                                    ((Loopable) captureSourceMap.captureSource).loop();
                                    DebugPrinter.println("Looping capture source");
                                } else {
                                    DebugPrinter.println(
                                            "Failed to read frame from CaptureSource "
                                            + captureSourceMap.captureSource);
                                }
                            } else {
                                for (int i = 0; i < captureSourceMap.modules.length; i++) {
                                    VisionModule module = captureSourceMap.modules[i];

                                    // Unless this is the last module, clone the frame, so
                                    // that modules can mutate the frame they are given.
                                    Mat uniqueFrame = i == captureSourceMap.modules.length - 1
                                        ? frame
                                        : frame.clone();

                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            long start = System.currentTimeMillis();
                                            module.run(uniqueFrame);
                                            long duration = System.currentTimeMillis() - start;
                                            DebugPrinter.println(module.getName() + " ran in " + duration + " ms");
                                        }
                                    }, module.getName() + " Thread");
                                    t.setDaemon(true);
                                    t.start();
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000 / FPS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "Module Runner Thread");
        t.start();
    }

    public ArrayList<VisionModule> getModules() {
        ArrayList<VisionModule> modules = new ArrayList<VisionModule>();
        for (CaptureSourceToVisionModuleMapper map : sourceDestMap) {
            for (VisionModule module : map.modules) {
                modules.add(module);
            }
        }
        return modules;
    }

    private static class CaptureSourceToVisionModuleMapper {
        private CaptureSource captureSource;
        private VisionModule[] modules;

        public CaptureSourceToVisionModuleMapper(CaptureSource captureSource, VisionModule... modules) {
            this.captureSource = captureSource;
            this.modules = modules;
        }
    }

}
