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
            KEY_STROKEORIENTATION = "STROKEORIENTATION",
            KEY_WAKELOCK = "WAKELOCK";

    /**
     * All of key lists used for {@link SharedPreferences}.<br>
     * Mainly used for iterating over all configurations.
     */
    public static final String[] KEY_ALL_CONFIGS = {
            KEY_STROKEORIENTATION,
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
        strokeOrientation = null;
        getStrokeOrientation();

        isWakeLockEnabled = null;
        isWakeLock();
    }

    // Stroke orientation
    public enum StrokeOrientation {
        /**
         * Stroke orientation - Left stroke means left; Right stroke means right.
         */
        NORMAL("Normal"),
        /**
         * Stroke orientation - Left stroke means right; Right stroke means left.
         */
        REVERSED("Reversed");

        private String name;

        StrokeOrientation(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static StrokeOrientation fromName(String name) {
            if (name != null) {
                for (StrokeOrientation value : StrokeOrientation.values()) {
                    if (name.equalsIgnoreCase(value.name)) {
                        return value;
                    }
                }
            }
            throw new IllegalArgumentException("No constant with text " + name + " found");
        }
    }

    private static StrokeOrientation strokeOrientation;

    /**
     * Get stroke orientation mode.
     * @return One of stroke orientation mode.<br>
     *          See {@link StrokeOrientation}
     */
    public StrokeOrientation getStrokeOrientation() {
        if (strokeOrientation == null) {
            String modeString = preferences.getString(KEY_STROKEORIENTATION,
                    StrokeOrientation.NORMAL.getName());
            strokeOrientation = StrokeOrientation.fromName(modeString);
            setStrokeOrientation(strokeOrientation);
        }
        return strokeOrientation;
    }

    /**
     * Set stroke orientation mode.
     * @param orientation One of stroke orientation mode.<br>
     *          See {@link StrokeOrientation}
     */
    public void setStrokeOrientation(StrokeOrientation orientation) {
        editor.putString(KEY_STROKEORIENTATION, orientation.getName()).commit();
        strokeOrientation = orientation;
    }

    // Wake lock
    private static Boolean isWakeLockEnabled;

    /**
     * Checks whether <i>Wake lock</i> is enabled.
     * @return true if <i>Wake lock</i> is enabled, false otherwise.
     */
    public boolean isWakeLock() {
        if (isWakeLockEnabled == null) {
            isWakeLockEnabled = preferences.getBoolean(KEY_WAKELOCK, false);
            setWakeLock(isWakeLockEnabled);
        }

        return isWakeLockEnabled;
    }

    /**
     * Set whether to use <i>Wake lock</i>.
     * @param isEnabled true to enable, false otherwise.
     */
    public void setWakeLock(boolean isEnabled) {
        editor.putBoolean(KEY_WAKELOCK, isEnabled).commit();
        isWakeLockEnabled = isEnabled;
    }

}
