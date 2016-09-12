package stuyvision.capture;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public abstract class CaptureSource {

    // To resize all frames to have, e.g., a width of 360px,
    // or a maximum dimension of some number of pixels.
    // See javadocs on methods such as readSized below.
    private ResizeDimension resizeDimension = ResizeDimension.maxDimension;
    private int resizeLength = 360;

    public abstract void reinitializeCaptureSource();

    public abstract boolean isOpened();

    /**
     * Read a frame into {@code mat}
     *
     * @param mat
     */
    public abstract boolean readFrame(Mat mat);

    /**
     * Reads a frame into {@code frame} and resizes it as specified by
     * the {@link #resizeDimensionTo(ResizeReferece resizeDim, int length)}
     * method into {@code outFrame}. {@code frame} is included to allow
     * you to more carefully manage memory usage with large frames.
     *
     * If {@code resizeDim} was set to {@code ResizeDimension.none}, reads
     * the frame into {@code outFrame} and does not interact with
     * {@code frame}.
     *
     * {@code frame} and {@code outFrame} may reference the same {@code Mat}.
     *
     * @param frame
     * @param outFrame
     *
     * @see #resizeDimensionTo(ResizeReferece resizeDim, int length)
     */
    public boolean readSized(Mat frame, Mat outFrame) {
        if (resizeDimension == ResizeDimension.none) {
            return readFrame(outFrame);
        }
        boolean success = readFrame(frame);
        if (success) {
            int frameHeight = frame.height();
            int frameWidth = frame.width();
            // Determine by how much to scale the image based on
            // the resizeDimension and resizeLength
            double resizeRatio;
            if (resizeDimension == ResizeDimension.maxDimension) {
                resizeRatio = ((double) resizeLength) / ((double) Math.max(frameHeight, frameWidth));
            } else if (resizeDimension == ResizeDimension.width) {
                resizeRatio = ((double) resizeLength) / ((double) frameWidth);
            } else {
                // resizeDimension is ResizeDimension.height
                resizeRatio = ((double) resizeLength) / ((double) frameHeight);
            }
            Size desiredSize = new Size(frameWidth * resizeRatio, frameHeight * resizeRatio);
            Imgproc.resize(frame, outFrame, desiredSize, 0, 0, Imgproc.INTER_CUBIC);
        }
        return success;
    }

    /**
     * @see #resizeDimensionTo(ResizeDimension, int)
     * @see #readSized(Mat, Mat)
     */
    public enum ResizeDimension {
        width, height, maxDimension, none
    }

    /**
     * @return The {@link ResizeDimension} to use when resizing input frames.
     * @see #resizeDimensionTo(ResizeDimension resizeDim, int length)
     * @see #getResizeLength()
     */
    public ResizeDimension getResizeDimension() {
        return resizeDimension;
    }

    /**
     * @return The length to set {@code resizeDimension} to when resizing input
     * frames.
     * @see #resizeDimensionTo(ResizeDimension resizeDim, int length)
     * @see #getResizeDimension()
     */
    public int getResizelength() {
        return resizeLength;
    }

    /**
     * Specify how to resize frames in {@code readSized}.
     *
     * {@link #readSized(Mat, Mat) readSized} will scale the frame
     * such that the dimension spcified by {@code resizeDim} has
     * length {@code length}
     *
     * @param resizeDim The dimension which will be set to {@code length}
     *     Values:
     *     <ul>
     *         <li>{@code ResizeDimension.width}: width dimension</li>
     *         <li>{@code ResizeDimension.height}: height dimension</li>
     *         <li>{@code ResizeDimension.maxDimension}: whichever dimension is
     *         bigger will be scaled to {@code length}</li>
     *         <li>{@code ResizeDimension.none}: never resize input frame (note
     *         that this may slow down processing significantly)</li>
     *     </ul>
     * @param length
     *
     * @see ResizeDimension
     * @see #readSized(Mat, Mat)
     */
    public void resizeDimensionTo(ResizeDimension resizeDim, int length) {
        resizeDimension = resizeDim;
        resizeLength = length;
    }
}
