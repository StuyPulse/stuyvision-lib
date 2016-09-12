package stuyvision.capture;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import stuyvision.util.FileManager;

public class ImageCaptureSource extends CaptureSource {

    private final String filename;
    private Mat mat = null;

    public ImageCaptureSource(String filename) {
        FileManager.assertFileExists(filename);
        this.filename = filename;
        reinitializeCaptureSource();
    }

    public ImageCaptureSource(String filename, CaptureSource.ResizeDimension dim, int length) {
        this(filename);
        resizeDimensionTo(dim, length);
    }

    @Override
    public void reinitializeCaptureSource() {
        mat = Imgcodecs.imread(filename);
    }

    @Override
    public boolean isOpened() {
        return mat != null;
    }

    @Override
    public boolean readFrame(Mat mat) {
        this.mat.copyTo(mat);
        return true;
    }

}
