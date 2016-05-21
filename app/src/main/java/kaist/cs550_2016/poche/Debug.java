package kaist.cs550_2016.poche;

import android.util.Log;
import android.widget.Toast;

/**
 * Created by Leegeun Ha on 2016-05-21.
 */

public class Debug {

    /**
     * Whether to use debug mode.<br>
     * In production, this variable must be set to false.
     */
    public static final boolean isDebug = true;

    /**
     * Wrapper for logging in debug mode.
     * To not be mixed with other logs, <br>
     * the log level is equivalent to {@link Log#w(String, String)}.
     * @param tag object/activity for identifying message
     * @param message message to be printed in log
     */
    public static void log(Object tag, String message) {
        if (!isDebug) return;
        Log.w(tag.getClass().getSimpleName(), message);
    }

    /**
     * Wrapper for showing {@link Toast} in debug mode.
     * @param message message to be printed in log
     * @param duration time length to be shown (See {@link Toast})
     */
    public static void toast(String message, int duration) {
        if (!isDebug) return;
        Toast.makeText(App.getAppContext(), message, duration).show();
    }

    /**
     * Wrapper for showing toast in debug mode in short duration.
     * @param message message to be printed in log
     */
    public static void toast(String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    /**
     * System time recorded for stopwatch utility.
     */
    private static long start;

    /**
     * Start a new stopwatch.
     */
    public static void stopwatchStart() {
        if (!isDebug) return;

        start = System.currentTimeMillis();
    }

    /**
     * Returns the elapsed CPU time (in seconds) since the stopwatch was started.
     * @return elapsed CPU time (in seconds) since the stopwatch was started
     */
    public static double stopwatchEnd() {
        if (!isDebug) return 0;

        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    }

    /**
     * Utility function for showing stopwatch value in {@link Toast}.<br>
     * Format: [[task]] + finished in [[time]] s.
     * @param task name for task showing in message.
     */
    public static void toastStopwatch(String task) {
        if (!isDebug) return;

        Debug.toast(task + " finished in " + Debug.stopwatchEnd() + " s.");
    }
}