package stuyvision.capture;

/**
 * Interface for subclasses of CaptureSource which can be looped.
 */
public interface Loopable {
    /**
     * Loop/reset to the first frame shown. Useful, e.g., for
     * VideoCaptureSource, in order to loop back to the beginning of a video
     * file upon reaching the end.
     *
     * @return Whether the operation succeeded.
     */
    public boolean loop();
}
