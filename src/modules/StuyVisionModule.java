package modules;

import util.Sender;

import vision.CaptureSource;
import vision.DeviceCaptureSource;
import vision.ImageCaptureSource;
import vision.VisionModule;

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

import gui.Main;

import java.util.Arrays;
import java.util.ArrayList;

import gui.IntegerSliderVariable;

public class StuyVisionModule extends VisionModule {
    public int minH_GREEN = 0;
    public int maxH_GREEN = 94;
    public int minS_GREEN = 88;
    public int maxS_GREEN = 255;
    public int minV_GREEN = 19;
    public int maxV_GREEN = 255;
    public int minH_GRAY = 0;
    public int maxH_GRAY = 255;
    public int minS_GRAY = 17;
    public int maxS_GRAY = 118;
    public int minV_GRAY = 120;
    public int maxV_GRAY = 244;
    public int threshBlockSizeH = 78;
    public int threshConstantH = 0;
    public boolean useH = true;
    public int threshBlockSizeS = 50;
    public int threshConstantS = 2;
    public boolean useS = false;
    public int threshBlockSizeV = 76;
    public int threshConstantV = 0;
    public boolean useV = true;
    public double AREA_THRESHOLD = 600.0;
    public double r1 = 1.25;
    public double r2 = 3.00;

    static {
        String dir = StuyVisionModule.class.getClassLoader().getResource("").getPath();
        //System.load(dir + "../lib/opencv-3.0.0/build/lib/libopencv_java300.so");
        // For running on Windows, use:
        //System.load(dir + "..\\lib\\opencv-3.0.0\\build\\lib\\opencv_java300.dll");
        System.load("C:\\Users\\Justin Time\\workspace\\stuy-vision-2016\\lib\\opencv-3.0.0\\build\\java\\x64\\opencv_java300.dll");
    }

    public static void main(String[] args) {
        System.out.println("Hello from modules.StuyVisionModule.main");

        CaptureSource cs = new DeviceCaptureSource(0);
        Sender sender = new Sender();
        StuyVisionModule vm = new StuyVisionModule();
        vm.processAndSendIndefinitely(cs, sender, true);
    }

    public void processAndSendIndefinitely(
            CaptureSource cs, Sender sender, boolean printInfo) {
        Mat frame = new Mat();
        for (;;) {
            cs.readFrame(frame);
            double[] vectorToGoal = hsvThresholding(frame);
            if (printInfo) {
                System.out.println("Sent vector: " + Arrays.toString(vectorToGoal));
            }
            sender.sendDoubles(vectorToGoal);
        }
    }

    public double printFilesystemSpeedTest(String path) {
        System.out.println("Running StuyVisionModule");
        StuyVisionModule vm = new StuyVisionModule();
        double avgTime = vm.filesystemTest(path, 10);
        System.out.println("Average time: " + avgTime);
        return avgTime;
    }

    public void printBytesSendTest(Sender sender, int numBytes) {
        System.out.println("About to send raw bytes to Tegra");
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < bytes.length; i += 1) {
            bytes[i] = (byte) (i + 65);
        }
        sender.sendData(bytes);
        System.out.println("Sent " + numBytes + " bytes");
    }

    public void printDoublesSendTest(Sender sender) {
        System.out.println("About to send doubles");
        double[] doubles = new double[10];
        for (int i = 0; i < doubles.length; i += 1) {
            doubles[i] = ((double) i) * 1.25;
        }
        sender.sendDoubles(doubles);
        System.out.println("Sent ten doubles");
    }

    public void processAndSend(Mat frame, Sender sender) {
        double[] vector = hsvThresholding(frame);
        sender.sendDoubles(vector);
    }

    private double cameraTest(int iters) {
        CaptureSource cs = new DeviceCaptureSource(0);
        return captureSourceTest(cs, iters);
    }

    private double filesystemTest(String path, int iters) {
        CaptureSource cs = new ImageCaptureSource(path);
        return captureSourceTest(cs, iters);
    }

    private double captureSourceTest(CaptureSource cs, int iters) {
        int total = 0;
        for (int i = 0; i < iters; i++) {
            Mat frame = new Mat();
            cs.readFrame(frame);
            long start = System.currentTimeMillis();
            hsvThresholding(frame);
            total += (int) (System.currentTimeMillis() - start);
        }
        return total / (double) iters;
    }

    private boolean aspectRatioThreshold(double height, double width) {
        double ratio = width / height;
        return (r1 < ratio && ratio < r2) || (1 / r2< ratio && ratio < 1 / r1);
    }

    private double[] getLargestGoal(Mat frame, Mat filteredImage, Main app) {
        boolean withGui = app != null;
        // Locate the goals
        Mat drawn = null;
        if (withGui) {
            drawn = frame.clone();
        }
        ArrayList<MatOfPoint> contour = new ArrayList<MatOfPoint>();
        Imgproc.findContours(filteredImage, contour, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        double largestArea = 0.0;
        RotatedRect largestRect = null;

        for (int i = 0; i < contour.size(); i++) {
            double currArea = Imgproc.contourArea(contour.get(i));
            if (currArea < AREA_THRESHOLD) {
                continue;
            }
            MatOfPoint2f tmp = new MatOfPoint2f();
            contour.get(i).convertTo(tmp, CvType.CV_32FC1);
            RotatedRect r = Imgproc.minAreaRect(tmp);
            if (!aspectRatioThreshold(r.size.height, r.size.width)) {
                continue;
            }
            if (withGui) {
                Point[] points = new Point[4];
                r.points(points);
                for (int line = 0; line < 4; line++) {
                    Imgproc.line(drawn, points[line], points[(line + 1) % 4], new Scalar(0, 255, 0));
                }
            }
            if (currArea > largestArea) {
                largestArea = currArea;
                largestRect = r;
            }
        }

        if (largestRect == null) {
            // Send three (+Infinity)s to signal that
            // nothing was found in the frame
            if (withGui) {
                app.postImage(frame, "Goals!", this);
            }
            return new double[] {6 / 0.0, 9 / 0.0, 4 / 0.0};
        }

        double[] vector = new double[3];
        vector[0] = largestRect.center.x - (double) (frame.width() / 2);
        vector[1] = largestRect.center.y - (double) (frame.height() / 2);
        vector[2] = largestRect.angle;

        if (withGui) {
            Imgproc.circle(drawn, largestRect.center, 1, new Scalar(0, 0, 255), 2);
            Imgproc.line(drawn, new Point(frame.width() / 2, frame.height() / 2), largestRect.center,
                    new Scalar(0, 0, 255));
            app.postImage(drawn, "Goals!", this);
        }

        return vector;
    }

    public double[] getLargestGoal(Mat frame, Mat filteredImage) {
        return getLargestGoal(frame, filteredImage, null);
    }

    public double[] hsvThresholding(Mat frame, Main app) {
        boolean withGui = app != null;
        Mat hsv = new Mat();
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV);
        ArrayList<Mat> greenFilterChannels = new ArrayList<Mat>();
        ArrayList<Mat> grayFilterChannels = new ArrayList<Mat>();

        // Split HSV channels and process each channel
        Core.split(hsv, greenFilterChannels);
        Core.inRange(greenFilterChannels.get(0), new Scalar(minH_GREEN), new Scalar(maxH_GREEN),
                greenFilterChannels.get(0));
        Core.inRange(greenFilterChannels.get(1), new Scalar(minS_GREEN), new Scalar(maxS_GREEN),
                greenFilterChannels.get(1));
        Core.inRange(greenFilterChannels.get(2), new Scalar(minV_GREEN), new Scalar(maxV_GREEN),
                greenFilterChannels.get(2));

        Core.split(hsv, grayFilterChannels);
        Core.inRange(grayFilterChannels.get(0), new Scalar(minH_GRAY), new Scalar(maxH_GRAY),
                grayFilterChannels.get(0));
        Core.inRange(grayFilterChannels.get(1), new Scalar(minS_GRAY), new Scalar(maxS_GRAY),
                grayFilterChannels.get(1));
        Core.inRange(grayFilterChannels.get(2), new Scalar(minV_GRAY), new Scalar(maxV_GRAY),
                grayFilterChannels.get(2));

        // Merge channels and erode dilate to remove noise
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9));

        Mat greenFiltered = new Mat();
        Core.bitwise_and(greenFilterChannels.get(0), greenFilterChannels.get(1), greenFiltered);
        Core.bitwise_and(greenFilterChannels.get(2), greenFiltered, greenFiltered);
        Imgproc.erode(greenFiltered, greenFiltered, erodeKernel);
        Imgproc.dilate(greenFiltered, greenFiltered, dilateKernel);

        Mat grayFiltered = new Mat();
        Core.bitwise_and(grayFilterChannels.get(0), grayFilterChannels.get(1), grayFiltered);
        Core.bitwise_and(grayFilterChannels.get(2), grayFiltered, grayFiltered);
        Imgproc.erode(grayFiltered, grayFiltered, erodeKernel);
        Imgproc.dilate(grayFiltered, grayFiltered, dilateKernel);

        if (withGui) {
            app.postImage(greenFiltered, "Green - After erode/dilate", this);
            app.postImage(grayFiltered, "Gray - After erode/dilate", this);
        }

        // Merge "grayed" reflexite with green reflexite
        Core.bitwise_or(greenFiltered, grayFiltered, greenFiltered);

        if (withGui) {
            app.postImage(greenFiltered, "Merged", this);
        }

        return getLargestGoal(frame, greenFiltered, app);
    }

    public double[] hsvThresholding(Mat frame) {
        return hsvThresholding(frame, null);
    }

    // For running as JavaFX gui
    public Object run(Main app, Mat frame) {
        app.postImage(frame, "Camera", this);
        return hsvThresholding(frame, app);
    }
}
