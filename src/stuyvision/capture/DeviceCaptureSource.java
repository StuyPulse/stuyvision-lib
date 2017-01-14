package stuyvision.capture;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class DeviceCaptureSource extends CaptureSource {

    private final int deviceNo;
    private VideoCapture capture;

    public DeviceCaptureSource(int device) {
        this.deviceNo = device;
        capture = new VideoCapture(device);
    }

    public DeviceCaptureSource(int device, CaptureSource.ResizeDimension dim, int length) {
        this(device);
        resizeDimensionTo(dim, length);
    }

    @Override
    public boolean isOpened() {
        return capture.isOpened();
    }

    @Override
    public boolean readFrame(Mat mat) {
        return capture.read(mat);
    }

    public void setExposure(int value) {
        capture.set(15, value);
    }

    public void setBrightness(int value) {
        capture.set(10, value);
    }

    public void setBufferSize(int bufferSize){
    	capture.set(Videoio.CV_CAP_PROP_BUFFERSIZE, bufferSize);
    }
}
