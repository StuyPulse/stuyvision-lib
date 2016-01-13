package modules;

import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import gui.DoubleSliderVariable;
import gui.IntegerSliderVariable;
import gui.Main;
import vision.VisionModule;

public class VisionModule1 extends VisionModule {
    public IntegerSliderVariable minH = new IntegerSliderVariable("Min H", 0,  0,  255);
    public IntegerSliderVariable maxH = new IntegerSliderVariable("Max H", 94,  0,  255);
    public IntegerSliderVariable minS = new IntegerSliderVariable("Min S", 156, 0, 255);
    public IntegerSliderVariable maxS = new IntegerSliderVariable("Max S", 255,  0,  255);
    public IntegerSliderVariable minV = new IntegerSliderVariable("Min V", 53,  0,  255);
    public IntegerSliderVariable maxV = new IntegerSliderVariable("Max V", 255, 0, 255);
    public DoubleSliderVariable AREA_THRESHOLD = new DoubleSliderVariable("AREA THRESH", 45.0, 0.0, 500.0);
    public boolean anglePrinted = false;

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
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.erode(channels.get(3), channels.get(3), erodeKernel);
        Imgproc.dilate(channels.get(3), channels.get(3), dilateKernel);
        app.postImage(channels.get(3), "After erode/dilate", this);

        // Locate the goals
        Mat drawn = frame.clone();
        Mat edges = new Mat();
        ArrayList<MatOfPoint> contour = new ArrayList<MatOfPoint>();
        Imgproc.findContours(channels.get(3), contour, new Mat() , Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double largestArea = 0.0;
        RotatedRect largestRect = new RotatedRect();
        for (int i = 0; i < contour.size(); i++) {
            System.out.println(Imgproc.contourArea(contour.get(i)));
        	if (Imgproc.contourArea(contour.get(i)) < AREA_THRESHOLD.value()) {
                continue;
            }
            MatOfPoint2f tmp = new MatOfPoint2f();
            contour.get(i).convertTo(tmp, CvType.CV_32FC1);
            RotatedRect r = Imgproc.minAreaRect(tmp);
            Point[] points = new Point[4];
            r.points(points);
            for (int line = 0; line < 4; line++) {
                Imgproc.line(drawn, points[line], points[(line + 1) % 4], new Scalar(0, 255, 0));
            }
        }
        app.postImage(channels.get(3), "Filtered HSV", this);
        app.postImage(drawn, "Goals!", this);
    }
}