package modules;

import java.util.ArrayList;

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

import gui.BooleanVariable;
import gui.DoubleSliderVariable;
import gui.IntegerSliderVariable;
import gui.Main;
import vision.VisionModule;

public class VisionModule1 extends VisionModule {
    public IntegerSliderVariable minH_GREEN = new IntegerSliderVariable("Min H Green", 0,  0,  255);
    public IntegerSliderVariable maxH_GREEN = new IntegerSliderVariable("Max H Green", 94,  0,  255);
    public IntegerSliderVariable minS_GREEN = new IntegerSliderVariable("Min S Green", 88, 0, 255);
    public IntegerSliderVariable maxS_GREEN = new IntegerSliderVariable("Max S Green", 255,  0,  255);
    public IntegerSliderVariable minV_GREEN = new IntegerSliderVariable("Min V Green", 19,  0,  255);
    public IntegerSliderVariable maxV_GREEN = new IntegerSliderVariable("Max V Green", 255, 0, 255);
    public IntegerSliderVariable minH_GRAY = new IntegerSliderVariable("Min H Gray", 0,  0,  255);
    public IntegerSliderVariable maxH_GRAY = new IntegerSliderVariable("Max H Gray", 255,  0,  255);
    public IntegerSliderVariable minS_GRAY = new IntegerSliderVariable("Min S Gray", 17, 0, 255);
    public IntegerSliderVariable maxS_GRAY = new IntegerSliderVariable("Max S Gray", 118,  0,  255);
    public IntegerSliderVariable minV_GRAY = new IntegerSliderVariable("Min V Gray", 120,  0,  255);
    public IntegerSliderVariable maxV_GRAY = new IntegerSliderVariable("Max V Gray", 244, 0, 255);
    public IntegerSliderVariable threshBlockSizeH = new IntegerSliderVariable(
            "Thresh Block SizeH", 78, 10, 100);
    public IntegerSliderVariable threshConstantH = new IntegerSliderVariable(
            "Thresh ConstantH", 0, 0, 200);
    public BooleanVariable useH = new BooleanVariable("Use H?", true);
    public IntegerSliderVariable threshBlockSizeS = new IntegerSliderVariable(
            "Thresh Block SizeS", 50, 10, 100);
    public IntegerSliderVariable threshConstantS = new IntegerSliderVariable(
            "Thresh ConstantS", 2, 0, 200);
    public BooleanVariable useS = new BooleanVariable("Use S?", false);
    public IntegerSliderVariable threshBlockSizeV = new IntegerSliderVariable(
            "Thresh Block SizeV", 76, 10, 100);
    public IntegerSliderVariable threshConstantV = new IntegerSliderVariable(
            "Thresh ConstantV", 0, 0, 200);
    public BooleanVariable useV = new BooleanVariable("Use V?", true);
    public DoubleSliderVariable AREA_THRESHOLD = new DoubleSliderVariable("AREA THRESH", 600.0, 0.0, 1000.0);
    public DoubleSliderVariable r1 = new DoubleSliderVariable("rat. min", 1.25, 1.0, 3.0);
    public DoubleSliderVariable r2 = new DoubleSliderVariable("rat. max", 2.20, 1.0, 3.0);
    
    class Bundle {
        ArrayList<MatOfPoint> contours;
        
        public Bundle() {
            contours = new ArrayList<>();
        }
    }

    private boolean aspectRatioThreshold(double height, double width) {
        double ratio = width / height;
        return (r1.value() < ratio && ratio < r2.value()) || (1 / r2.value()< ratio && ratio < 1 / r1.value());
    }

    private double[] getLargestGoal(Main app, Mat frame, ArrayList<Mat> channels) {
            // Locate the goals
            Mat drawn = frame.clone();
            ArrayList<MatOfPoint> contour = new ArrayList<MatOfPoint>();
            Imgproc.findContours(channels.get(3), contour, new Mat() , Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            double largestArea = 0.0;
            RotatedRect largestRect = new RotatedRect();

            for (int i = 0; i < contour.size(); i++) {
                double currArea = Imgproc.contourArea(contour.get(i));
                if (currArea < AREA_THRESHOLD.value()) {
                    continue;
                }
                MatOfPoint2f tmp = new MatOfPoint2f();
                contour.get(i).convertTo(tmp, CvType.CV_32FC1);
                RotatedRect r = Imgproc.minAreaRect(tmp);
                if (!aspectRatioThreshold(r.size.height, r.size.width)) {
                    continue;
                }
                Point[] points = new Point[4];
                r.points(points);
                for (int line = 0; line < 4; line++) {
                    Imgproc.line(drawn, points[line], points[(line + 1) % 4], new Scalar(0, 255, 0));
                }
                if (currArea > largestArea) {
                    largestArea = currArea;
                    largestRect = r;
                }
            }
            Imgproc.circle(drawn, largestRect.center, 1, new Scalar(0, 0, 255), 2);
            double[] vector = new double[3];
            vector[0] = largestRect.center.x - (double)(frame.width() / 2);
            vector[1] = largestRect.center.y - (double)(frame.height() / 2);
            Imgproc.line(drawn, new Point(frame.width() / 2, frame.height() / 2), largestRect.center, new Scalar(0, 0, 255));
            vector[2] = largestRect.angle;
            app.postImage(drawn, "Goals!", this);
            return vector;
    }

    public double[] hsvThresholding(Main app, Mat frame) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        ArrayList<Mat> greenFilterChannels = new ArrayList<Mat>();
        ArrayList<Mat> grayFilterChannels = new ArrayList<Mat>();

        // Split HSV channels and process each channel
        Core.split(hsv, greenFilterChannels);
        Core.inRange(greenFilterChannels.get(0), new Scalar(minH_GREEN.value()), new Scalar(maxH_GREEN.value()),
                greenFilterChannels.get(0));
        Core.inRange(greenFilterChannels.get(1), new Scalar(minS_GREEN.value()), new Scalar(maxS_GREEN.value()),
                greenFilterChannels.get(1));
        Core.inRange(greenFilterChannels.get(2), new Scalar(minV_GREEN.value()), new Scalar(maxV_GREEN.value()),
                greenFilterChannels.get(2));
        greenFilterChannels.add(new Mat());

        Core.split(hsv, grayFilterChannels);
        Core.inRange(grayFilterChannels.get(0), new Scalar(minH_GRAY.value()), new Scalar(maxH_GRAY.value()),
                grayFilterChannels.get(0));
        Core.inRange(grayFilterChannels.get(1), new Scalar(minS_GRAY.value()), new Scalar(maxS_GRAY.value()),
                grayFilterChannels.get(1));
        Core.inRange(grayFilterChannels.get(2), new Scalar(minV_GRAY.value()), new Scalar(maxV_GRAY.value()),
                grayFilterChannels.get(2));
        grayFilterChannels.add(new Mat());

        // Merge channels and erode dilate to remove noise
        Core.bitwise_and(greenFilterChannels.get(0), greenFilterChannels.get(1), greenFilterChannels.get(3));
        Core.bitwise_and(greenFilterChannels.get(2), greenFilterChannels.get(3), greenFilterChannels.get(3));
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9));
        Imgproc.erode(greenFilterChannels.get(3), greenFilterChannels.get(3), erodeKernel);
        Imgproc.dilate(greenFilterChannels.get(3), greenFilterChannels.get(3), dilateKernel);

        Core.bitwise_and(grayFilterChannels.get(0), grayFilterChannels.get(1), grayFilterChannels.get(3));
        Core.bitwise_and(grayFilterChannels.get(2), grayFilterChannels.get(3), grayFilterChannels.get(3));
        Imgproc.erode(grayFilterChannels.get(3), grayFilterChannels.get(3), erodeKernel);
        Imgproc.dilate(grayFilterChannels.get(3), grayFilterChannels.get(3), dilateKernel);

        app.postImage(greenFilterChannels.get(3), "Green - After erode/dilate", this);
        app.postImage(grayFilterChannels.get(3), "Gray - After erode/dilate", this);

        // Merge "grayed" reflexite with green reflexite
        Core.bitwise_or(greenFilterChannels.get(3), grayFilterChannels.get(3), greenFilterChannels.get(3));
        app.postImage(greenFilterChannels.get(3), "Merged", this);

        return getLargestGoal(app, frame, greenFilterChannels);
    }

    public double[] adaptiveThresh(Main app, Mat frame) {
        Mat hsv = new Mat();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        ArrayList<Mat> adaptiveChans = new ArrayList<Mat>();
        Core.split(hsv, adaptiveChans);

        // Adaptive Thresholding
        Imgproc.adaptiveThreshold(adaptiveChans.get(0), adaptiveChans.get(0),
                255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV,
                2 * threshBlockSizeH.value() + 1, threshConstantH.value());
        Imgproc.adaptiveThreshold(adaptiveChans.get(1), adaptiveChans.get(1),
                255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV,
                2 * threshBlockSizeS.value() + 1, threshConstantS.value());
        Imgproc.adaptiveThreshold(adaptiveChans.get(2), adaptiveChans.get(2),
                255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,
                2 * threshBlockSizeV.value() + 1, threshConstantV.value());
        adaptiveChans.add(new Mat(frame.rows(), frame.cols(), CvType.CV_8UC3, new Scalar(255, 255, 255)));
        Imgproc.cvtColor(adaptiveChans.get(3), adaptiveChans.get(3), Imgproc.COLOR_BGR2GRAY);
        // Use H and V channels!
        if (useH.getValue()) Core.bitwise_and(adaptiveChans.get(0), adaptiveChans.get(3), adaptiveChans.get(3));
        if (useS.getValue()) Core.bitwise_and(adaptiveChans.get(1), adaptiveChans.get(3), adaptiveChans.get(3));
        if (useV.getValue()) Core.bitwise_and(adaptiveChans.get(2), adaptiveChans.get(3), adaptiveChans.get(3));
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7, 7));
        Imgproc.erode(adaptiveChans.get(3), adaptiveChans.get(3), erodeKernel);
        Imgproc.dilate(adaptiveChans.get(3), adaptiveChans.get(3), dilateKernel);
        // app.postImage(adaptiveChans.get(0), "Adaptive Thresh H", this);
        // app.postImage(adaptiveChans.get(1), "Adaptive Thresh S", this);
        // app.postImage(adaptiveChans.get(2), "Adaptive Thresh V", this);
        // app.postImage(adaptiveChans.get(3), "Adaptive Thresh", this);
        return getLargestGoal(app, frame, adaptiveChans);
    }

    public Object run(Main app, Mat frame) {
        app.postImage(frame, "Camera", this);
        // return adaptiveThresh(app, frame);
        return hsvThresholding(app, frame);
    }
}