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
import org.opencv.videoio.VideoCapture;

public class VisionModuleNoGui {
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

    public static void main(String[] args) {
	System.out.println("Hello from modules.VisionModuleNoGui.main");
    }

    private int cameraTest(int iters) {
    	VideoCapture vc = new VideoCapture(0);
    	Mat frame = new Mat();
    	
    	int total = 0;
    	for (int i = 0; i < iters; i++) {
    		long start = System.currentTimeMillis();
        	vc.read(frame);
    		hsvThresholding(frame);
    		long duration = System.currentTimeMillis() - start;
    		total += (int)duration;
    	}
    	return total / iters;
    }
    
	private boolean aspectRatioThreshold(double height, double width) {
        double ratio = width / height;
        return (r1 < ratio && ratio < r2) || (1 / r2< ratio && ratio < 1 / r1);
    }

    private double[] getLargestGoal(Mat frame, ArrayList<Mat> channels) {
            // Locate the goals
            Mat drawn = frame.clone();
            ArrayList<MatOfPoint> contour = new ArrayList<MatOfPoint>();
            Imgproc.findContours(channels.get(3), contour, new Mat() , Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            double largestArea = 0.0;
            RotatedRect largestRect = new RotatedRect();

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
            //app.postImage(drawn, "Goals!", this);
            return vector;
    }

    public double[] hsvThresholding(Mat frame) {
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
        greenFilterChannels.add(new Mat());

        Core.split(hsv, grayFilterChannels);
        Core.inRange(grayFilterChannels.get(0), new Scalar(minH_GRAY), new Scalar(maxH_GRAY),
                grayFilterChannels.get(0));
        Core.inRange(grayFilterChannels.get(1), new Scalar(minS_GRAY), new Scalar(maxS_GRAY),
                grayFilterChannels.get(1));
        Core.inRange(grayFilterChannels.get(2), new Scalar(minV_GRAY), new Scalar(maxV_GRAY),
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

        //app.postImage(greenFilterChannels.get(3), "Green - After erode/dilate", this);
        //app.postImage(grayFilterChannels.get(3), "Gray - After erode/dilate", this);

        // Merge "grayed" reflexite with green reflexite
        Core.bitwise_or(greenFilterChannels.get(3), grayFilterChannels.get(3), greenFilterChannels.get(3));
        //app.postImage(greenFilterChannels.get(3), "Merged", this);

        return getLargestGoal(frame, greenFilterChannels);
    }
}
