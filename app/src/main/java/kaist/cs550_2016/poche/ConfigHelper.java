package kaist.cs550_2016.poche;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;

/**
 * Created by Leegeun Ha.
 */

/**
 * Singleton class that manages configuration stored in application.<br>
 * Configurations can be set, or retrieved through methods.<br><br>
 *
 * Developers Note: Steps to add a new configuration item<br>
 * 1. Add Key name used in {@link SharedPreferences}, in private static final String.<br>
 * 2. Add a string created in step 1. to {@link ConfigHelper#KEY_ALL_CONFIGS}.<br>
 * 3. Declare a variable to maintain configuration value, in private static [TYPE] form.<br>
 * 4. If there is no initial value for decleared variable in step 3,<br> create getter method
 *    get[VARIABLE NAME] that retrieves persistent configuration from {@link SharedPreferences}.<br>
 *    <b>IMPORTANT: Default value must be given and set if not present.</b>
 */
public class ConfigHelper {

    private static final String
            KEY_SWIPEDIRECTION = "SWIPEDIRECTION",
            KEY_WAKELOCK = "WAKELOCK";

    /**
     * Swipe direction - Left swipe means left; Right swipe means right.
     */
    public static final String SWIPEDIRECTION_NORMAL = "Normal";
    /**
     * Swipe direction - Left swipe means right; Right swipe means left.
     */
    public static final String SWIPEDIRECTION_REVERSED = "Reversed";

    private static final String[] SWIPEDIRECTION_ALL = {
            SWIPEDIRECTION_NORMAL,
            SWIPEDIRECTION_REVERSED
    };

    /**
     * All of key lists used for {@link SharedPreferences}.<br>
     * Mainly used for iterating over all configurations.
     */
    public static final String[] KEY_ALL_CONFIGS = {
            KEY_SWIPEDIRECTION,
            KEY_WAKELOCK
    };

    private static ConfigHelper ourInstance;

    /**
     * Prepare configuration from {@link SharedPreferences} and initialize Singleton instance.
     * @param context Android {@link Context} for using {@link SharedPreferences}
     */
    public static void init(Context context) {
        if (ourInstance != null) return;
        ourInstance = new ConfigHelper();
        ourInstance.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ourInstance.editor = ourInstance.preferences.edit();
        ourInstance.syncPreferences();
    }

    /**
     * Get current singleton instance.
     * @return singleton instance
     */
    public static ConfigHelper getInstance() {
        return ourInstance;
    }

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private ConfigHelper() {
    }

    /**
     * Get {@link SharedPreferences} managed by this class.<br>
     * After editing, {@link ConfigHelper#syncPreferences()} must be called.
     */
    public SharedPreferences getSharedPreferences() {
        return preferences;
    }

    /**
     * Synchronize {@link SharedPreferences} and attributes of this instance.<br>
     * This also fills default value if there is no configuration.
     */
    public void syncPreferences() {
        swipeDirection = null;
        getSwipeDirection();

        isWakeLockEnabled = null;
        isWakeLockEnabled();
    }

    // Swipe direction
    private static String swipeDirection;

    /**
     * Get swipe direction mode.
     * @return One of swipe direction mode.<br>
     *          {@link ConfigHelper#SWIPEDIRECTION_NORMAL},<br>
     *          {@link ConfigHelper#SWIPEDIRECTION_REVERSED}.
     */
    public String getSwipeDirection() {
        if (swipeDirection == null) {
            swipeDirection = preferences.getString(KEY_SWIPEDIRECTION, SWIPEDIRECTION_NORMAL);
            setSwipeDirection(swipeDirection);
        }
        return swipeDirection;
    }

    /**
     * Set swipe direction mode.
     * @param direction One of swipe direction mode.<br>
     *          {@link ConfigHelper#SWIPEDIRECTION_NORMAL},<br>
     *          {@link ConfigHelper#SWIPEDIRECTION_REVERSED}<br>
     *          Other values will be ignored.
     */
    public void setSwipeDirection(String direction) {
        if (Arrays.asList(SWIPEDIRECTION_ALL).contains(direction)) {
            editor.putString(KEY_SWIPEDIRECTION, direction).commit();
            swipeDirection = direction;
        }
    }

    // Wake lock
    private static Boolean isWakeLockEnabled;

    /**
     * Checks whether <i>Wake lock</i> is enabled.
     * @return true if <i>Wake lock</i> is enabled, false otherwise.
     */
    public boolean isWakeLockEnabled() {
        if (isWakeLockEnabled == null) {
            isWakeLockEnabled = preferences.getBoolean(KEY_WAKELOCK, false);
            setWakeLockEnabled(isWakeLockEnabled);
        }

        return isWakeLockEnabled;
    }

    /**
     * Set whether to use <i>Wake lock</i>.
     * @param isEnabled true to enable, false otherwise.
     */
    public void setWakeLockEnabled(boolean isEnabled) {
        editor.putBoolean(KEY_WAKELOCK, isEnabled).commit();
        isWakeLockEnabled = isEnabled;
    }

}
