package modules;

import java.io.PrintWriter;
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
import util.ClientSocket;
import util.TegraServer;
import vision.CaptureSource;
import vision.DeviceCaptureSource;
import vision.ImageCaptureSource;
import vision.VisionModule;

public class StuyVisionModule extends VisionModule {
    public IntegerSliderVariable minH_GREEN = new IntegerSliderVariable("Min H Green", 58,  0,  255);
    public IntegerSliderVariable maxH_GREEN = new IntegerSliderVariable("Max H Green", 123,  0,  255);
    public IntegerSliderVariable minS_GREEN = new IntegerSliderVariable("Min S Green", 104, 0, 255);
    public IntegerSliderVariable maxS_GREEN = new IntegerSliderVariable("Max S Green", 255,  0,  255);
    public IntegerSliderVariable minV_GREEN = new IntegerSliderVariable("Min V Green", 20,  0,  255);
    public IntegerSliderVariable maxV_GREEN = new IntegerSliderVariable("Max V Green", 155, 0, 255);
    public IntegerSliderVariable minH_GRAY = new IntegerSliderVariable("Min H Gray", 60,  0,  255);
    public IntegerSliderVariable maxH_GRAY = new IntegerSliderVariable("Max H Gray", 197,  0,  255);
    public IntegerSliderVariable minS_GRAY = new IntegerSliderVariable("Min S Gray", 76, 0, 255);
    public IntegerSliderVariable maxS_GRAY = new IntegerSliderVariable("Max S Gray", 255,  0,  255);
    public IntegerSliderVariable minV_GRAY = new IntegerSliderVariable("Min V Gray", 0,  0,  255);
    public IntegerSliderVariable maxV_GRAY = new IntegerSliderVariable("Max V Gray", 244, 0, 255);
    public int threshBlockSizeH = 78;
    public int threshConstantH = 0;
    public boolean useH = true;
    public int threshBlockSizeS = 50;
    public int threshConstantS = 2;
    public boolean useS = false;
    public int threshBlockSizeV = 76;
    public int threshConstantV = 0;
    public boolean useV = true;
    private static double frameWidth = 640.0;
    private static double frameHeight = 480.0;
    private static double frameArea = frameWidth * frameHeight;
    public DoubleSliderVariable minAreaThreshold = new DoubleSliderVariable("Min Area Threshold", 200.0, 0.0, 700.0);
    public DoubleSliderVariable maxAreaThreshold = new DoubleSliderVariable("Max Area Threshold", frameArea * 0.1, 0.0, frameArea);
    public DoubleSliderVariable r1 = new DoubleSliderVariable("Min ratio theshold", 1.1, 0.0, 4.0);
    public DoubleSliderVariable r2 = new DoubleSliderVariable("Max ratio theshold", 3.0, 0.0, 4.0);

    private static PrintWriter writer = null;

    static {
        String dir = StuyVisionModule.class.getClassLoader().getResource("").getPath();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			System.load(dir + "..\\lib\\opencv-3.0.0\\build\\lib\\opencv_java300.dll");
        } else {
			System.load(dir + "../lib/opencv-3.0.0/build/lib/libopencv_java300.so");
        }
        try {writer = new PrintWriter("logs.txt");} catch (Exception e) {}
    }

    public static void main(String[] args) {
        System.out.println("Hello from modules.StuyVisionModule.main");

        CaptureSource cs = new DeviceCaptureSource(0);
        System.out.println("Got camera");
        TegraServer server = new TegraServer();
        StuyVisionModule vm = new StuyVisionModule();
        vm.processAndSendIndefinitely(cs, server, true);
    }
    // Camera constants
    static double MAX_DEGREES_OFF_AUTO_AIMING = 2;
    static int CAMERA_FRAME_PX_WIDTH = 1280;
    static int CAMERA_FRAME_PX_HEIGHT = 720;
    static int CAMERA_VIEWING_ANGLE_X = 100; // This is most likely wrong

    private static double pxOffsetToDegrees(double px) {
        return CAMERA_VIEWING_ANGLE_X * px / CAMERA_FRAME_PX_WIDTH;
    }

    private static void printVectorInfo(double[] vectorToGoal, PrintWriter writer) {
        writer.println("\n\n======================================");
        writer.println("" + System.currentTimeMillis());
        writer.println("Vector: " + Arrays.toString(vectorToGoal));
        writer.flush();
        if (vectorToGoal[0] == Double.POSITIVE_INFINITY
                && vectorToGoal[1] == Double.POSITIVE_INFINITY
                && vectorToGoal[2] == Double.POSITIVE_INFINITY) {
            writer.println("NO GOAL IN FRAME");
            writer.flush();
            return;
        }
        double degsOff = pxOffsetToDegrees(vectorToGoal[0]);
        writer.println("Degree offest to account for: " + degsOff);
        if (Math.abs(degsOff) < MAX_DEGREES_OFF_AUTO_AIMING) {
            writer.println("CLOSE ENOUGH. SHOOT.");
            writer.flush();
            return;
        }
        double rightWheelSpeed = -degsOff / (CAMERA_FRAME_PX_WIDTH / 2);
        writer.println("MOVE MOTORS AS SUCH: (" + -rightWheelSpeed + ", " + rightWheelSpeed + ")");
        writer.flush();
    }

    public void processAndSendIndefinitely(
            CaptureSource cs, TegraServer server, boolean printInfo) {
        Mat frame = new Mat();
        for (;;) {
            cs.readFrame(frame);
            double[] vectorToGoal = hsvThresholding(frame);
            if (printInfo) {
                System.out.println("Sent vector: " + Arrays.toString(vectorToGoal));
            }
            server.sendDoubles(vectorToGoal);
        }
    }

    public double printFilesystemSpeedTest(String path) {
        System.out.println("Running StuyVisionModule");
        StuyVisionModule vm = new StuyVisionModule();
        double avgTime = vm.filesystemTest(path, 10);
        System.out.println("Average time: " + avgTime);
        return avgTime;
    }

    public void printDoublesSendTest(ClientSocket socket) {
        System.out.println("About to send doubles");
        double[] doubles = new double[10];
        for (int i = 0; i < doubles.length; i += 1) {
            doubles[i] = ((double) i) * 1.25;
        }
        socket.sendDoubles(doubles);
        System.out.println("Sent ten doubles");
    }

    public void processAndSend(Mat frame, ClientSocket socket) {
        double[] vector = hsvThresholding(frame);
        socket.sendDoubles(vector);
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
        return (r1.value() < ratio && ratio < r2.value()) || (1 / r2.value() < ratio && ratio < 1 / r1.value());
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
            if (currArea < minAreaThreshold.value() || currArea > maxAreaThreshold.value()) {
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

        // Split HSV channels and process each channel
        Core.split(hsv, greenFilterChannels);
        Core.inRange(greenFilterChannels.get(0), new Scalar(minH_GREEN.value()), new Scalar(maxH_GREEN.value()),
                greenFilterChannels.get(0));
        Core.inRange(greenFilterChannels.get(1), new Scalar(minS_GREEN.value()), new Scalar(maxS_GREEN.value()),
                greenFilterChannels.get(1));
        Core.inRange(greenFilterChannels.get(2), new Scalar(minV_GREEN.value()), new Scalar(maxV_GREEN.value()),
                greenFilterChannels.get(2));

        // Merge channels and erode dilate to remove noise
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9));

        Mat greenFiltered = new Mat();
        Core.bitwise_and(greenFilterChannels.get(0), greenFilterChannels.get(1), greenFiltered);
        Core.bitwise_and(greenFilterChannels.get(2), greenFiltered, greenFiltered);
        Imgproc.erode(greenFiltered, greenFiltered, erodeKernel);
        Imgproc.dilate(greenFiltered, greenFiltered, dilateKernel);

        if (withGui) {
            app.postImage(greenFiltered, "Green - After erode/dilate", this);
            app.postImage(greenFiltered, "Merged", this);
        }

        double[] output = getLargestGoal(frame, greenFiltered, app);
        try {
            writer.println("Vector calculated: " + Arrays.toString(output));
            writer.flush();
        } catch (Exception e) {}
        return output;
    }

    public double[] hsvThresholding(Mat frame) {
        return hsvThresholding(frame, null);
    }

    // For running as JavaFX gui
    public Object run(Main app, Mat frame) {
        app.postImage(frame, "Camera", this);
        double[] vectorToGoal = hsvThresholding(frame, app);
        printVectorInfo(vectorToGoal, writer);
        return vectorToGoal;
    }
}
