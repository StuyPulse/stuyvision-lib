package stuyvision;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import stuyvision.VisionModule;
import stuyvision.capture.CaptureSource;
import stuyvision.capture.Loopable;
import stuyvision.util.DebugPrinter;

/**
 * A {@code ModuleRunner} holds a set of mappings from one
 * {@link stuyvision.capture.CaptureSource} to one or more {@link VisionModule}s.
 *
 * The {@code ModuleRunner} is run with {@code ModuleRunner.run()}, spawning
 * a thread which executes the {@link stuyvision.VisionModule#run(Mat)} method
 * on each {@code VisionModule}, passing a {@code Mat} read from the
 * corresponding {@code CaptureSource}. It will loop
 * {@link stuyvision.capture.Loopable} {@code CaptureSource}s once they fail to
 * read a frame.
 *
 * @see stuyvision.gui.VisionGui
 * @see stuyvision.gui.VisionGui#begin(String[], stuyvision.ModuleRunner)
 */
public class ModuleRunner {
    private static final int DEFAULT_FPS = 10;

    private ArrayList<CaptureSourceToVisionModuleMapper> sourceDestMap;
    private int fps;

    /**
     * The framerate to be used by {@link #run()} defaults to
     * {@value #DEFAULT_FPS}fps.
     *
     * @see #ModuleRunner(int fps) to specify a different framerate
     */
    public ModuleRunner() {
        sourceDestMap = new ArrayList<CaptureSourceToVisionModuleMapper>();
        fps = DEFAULT_FPS;
    }

    /**
     * @param fps The framerate to be used by {@link #run()}
     */
    public ModuleRunner(int fps) {
        sourceDestMap = new ArrayList<CaptureSourceToVisionModuleMapper>();
        this.fps = fps;
    }

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
                        Thread.sleep(1000 / fps);
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
