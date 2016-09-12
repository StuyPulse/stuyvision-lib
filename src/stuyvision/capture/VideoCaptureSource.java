package stuyvision.capture;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import static org.opencv.videoio.Videoio.CAP_PROP_POS_FRAMES;

import stuyvision.util.DebugPrinter;
import stuyvision.util.FileManager;

public class VideoCaptureSource extends CaptureSource implements Loopable {

    private final String filename;
    private VideoCapture capture = null;

    public VideoCaptureSource(String filename) {
        FileManager.assertFileExists(filename);
        this.filename = filename;
        reinitializeCaptureSource();
    }

    public VideoCaptureSource(String filename, CaptureSource.ResizeDimension dim, int length) {
        this(filename);
        resizeDimensionTo(dim, length);
    }

    /**
     * Reintialize the underlying VideoCapture.
     */
    public void reinitializeCaptureSource() {
        if (capture != null) {
            capture.release();
        }
        capture = new VideoCapture(filename);
    }

    @Override
    public boolean isOpened() {
        return capture.isOpened();
    }

    @Override
    public boolean readFrame(Mat mat) {
        return capture.read(mat);
    }

    @Override
    public boolean loop() {
        if (capture == null) {
            return false;
        }
        boolean success = capture.set(CAP_PROP_POS_FRAMES, 0);
        // FIXME: Determine if OpenCV issue with setting CAP_PROP_POS_FRAMES
        // on video formats with high compression is still relevant in 3.0.0.
        // Relevant: code.opencv.org/issues/1419 , stackoverflow.com/a/36402322

        if (!success) {
            DebugPrinter.println(
                    "VideoCaptureSource.loop: Setting VideoCapture frame "
                    + "to first frame of video failed. Falling back on "
                    + "reinitializeCaptureSource in order to loop");
            reinitializeCaptureSource();
        }
        return true;
    }

}
