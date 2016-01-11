package modules;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import gui.Main;
import vision.IntegerSliderVariable;
import vision.VisionModule;

public class VisionModule1 extends VisionModule {
    private static final double AREA_THRESHOLD = 30.0;

    public IntegerSliderVariable minH = new IntegerSliderVariable("Min H", 0,  0,  255);
    public IntegerSliderVariable maxH = new IntegerSliderVariable("Max H", 94,  0,  255);
    public IntegerSliderVariable minS = new IntegerSliderVariable("Min S", 156, 0, 255);
    public IntegerSliderVariable maxS = new IntegerSliderVariable("Max S", 255,  0,  255);
    public IntegerSliderVariable minV = new IntegerSliderVariable("Min V", 53,  0,  255);
    public IntegerSliderVariable maxV = new IntegerSliderVariable("Max V", 255, 0, 255);
    public void run(Main app, Mat frame) {
        app.postImage(frame, "Camera", this);
        Mat hsv = new Mat();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        ArrayList<Mat> channels = new ArrayList<Mat>();
        Core.split(hsv, channels);
        Core.inRange(channels.get(0), new Scalar(minH.value()), new Scalar(maxH.value()),
                channels.get(0));
        Core.inRange(channels.get(1), new Scalar(minS.value()), new Scalar(maxS.value()),
                channels.get(1));
        Core.inRange(channels.get(2), new Scalar(minV.value()), new Scalar(maxV.value()),
                channels.get(2));
        channels.add(new Mat());
        Core.bitwise_and(channels.get(0), channels.get(1), channels.get(3));
        Core.bitwise_and(channels.get(2), channels.get(3), channels.get(3));
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.erode(channels.get(3), channels.get(3), erodeKernel);
        Imgproc.dilate(channels.get(3), channels.get(3), dilateKernel);
        
        // Locate the goals
        Mat drawn = frame.clone();
        Mat edges = new Mat();
        ArrayList<MatOfPoint> contour = new ArrayList<MatOfPoint>();
        Imgproc.Canny(channels.get(3), edges, 0, 100);
        Imgproc.findContours(edges, contour, new Mat() , Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int i = 0; i < contour.size(); i++) {
            if (Imgproc.contourArea(contour.get(i)) < AREA_THRESHOLD) {
                continue;
            }
            Rect r = Imgproc.boundingRect(contour.get(i));
            Imgproc.rectangle(drawn, r.tl(), r.br(), new Scalar(0, 255, 0));
        }
        app.postImage(channels.get(3), "Filtered HSV", this);
        app.postImage(drawn, "Goals!", this);
    }
}