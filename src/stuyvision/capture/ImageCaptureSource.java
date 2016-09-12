package stuyvision.capture;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import stuyvision.util.FileManager;

public class ImageCaptureSource extends CaptureSource {

    private final String filename;
    private Mat mat;

    public ImageCaptureSource(String filename) {
        FileManager.assertFileExists(filename);
        this.filename = filename;
        mat = Imgcodecs.imread(filename);
    }

    public ImageCaptureSource(String filename, CaptureSource.ResizeDimension dim, int length) {
        this(filename);
        resizeDimensionTo(dim, length);
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
