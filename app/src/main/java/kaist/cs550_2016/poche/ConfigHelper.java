package kaist.cs550_2016.poche;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import junit.framework.Assert;

/**
 * Created by Leegeun Ha.
 */

/**
 * Singleton class that manages configuration stored in application.<br>
 * Configurations can be set, or retrieved through methods.<br><br>
 *
 * Developers Note: Steps to add a new configuration item<br>
 * 1. Add Key name used in {@link SharedPreferences}, in private static final String.<br>
 * 2. Add a string created in step 1. to {@link ConfigHelper#KEY_ALL_CONFIGS_IN_CONFIGACTIVITY}.<br>
 * 3. Declare a variable to maintain configuration value, in private static [TYPE] form.<br>
 * 4. If there is no initial value for decleared variable in step 3,<br> create getter method
 *    get[VARIABLE NAME] that retrieves persistent configuration from {@link SharedPreferences}.<br>
 *    <b>IMPORTANT: Default value must be given and set if not present.</b>
 */
public class ConfigHelper {

    private static final String
            KEY_STROKEORIENTATION = "STROKEORIENTATION",
            KEY_WAKELOCK = "WAKELOCK",
            KEY_STROKEPULL = "STROKEPULL",
            KEY_PLAYORDER = "PLAYORDER";

    /**
     * All of key lists used for {@link SharedPreferences}.<br>
     * Mainly used for iterating items in {@link ConfigActivity}
     */
    public static final String[] KEY_ALL_CONFIGS_IN_CONFIGACTIVITY = {
            KEY_PLAYORDER,
            KEY_STROKEORIENTATION,
            KEY_STROKEPULL,
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

        isStrokePullEnabled = null;
        isStrokePull();

        playOrder = null;
        getPlayOrder();
    }

    // Play order mode
    public enum PlayOrder {
        /**
         * Play order - Sequential order defined by playlist.
         */
        ORDERED,
        /**
         * Play order - Random order defined differently every loading.
         */
        SHUFFLE
    }

    private PlayOrder playOrder;

    /**
     * Get play order mode.
     * @return One of play order mode.<br>
     *          See {@link PlayOrder}
     */
    public PlayOrder getPlayOrder() {
        if (playOrder == null) {
            String modeString = preferences.getString(KEY_PLAYORDER,
                    PlayOrder.ORDERED.toString());
            playOrder = PlayOrder.valueOf(modeString);
            setPlayOrder(playOrder);
        }
        return playOrder;
    }

    /**
     * Set play order mode.
     * @param playOrder One of play order mode.<br>
     *          See {@link PlayOrder}
     */
    public void setPlayOrder(PlayOrder playOrder) {
        editor.putString(KEY_PLAYORDER, playOrder.toString()).commit();
        this.playOrder = playOrder;
    }

    // Stroke orientation
    public enum StrokeOrientation {
        NORTH(0),
        EAST(90),
        SOUTH(180),
        WEST(270);

        private int orientation;

        StrokeOrientation(int nv) {
            orientation = nv;
        }

        public int getDegree() {
            return this.orientation;
        }
        public String toString() {
            switch (this.orientation) {
                case 0:
                    return "North";
                case 90:
                    return "East";
                case 180:
                    return "South";
                case 270:
                    return "West";
            }
            Assert.assertNotNull(null);;
            return "Invalid ENUM";
        }
    }

    private StrokeOrientation strokeOrientation;

    /**
     * Get stroke orientation mode.
     * @return One of stroke orientation mode.<br>
     *          See {@link StrokeOrientation}
     */
    public StrokeOrientation getStrokeOrientation() {
        if (strokeOrientation == null) {
            strokeOrientation = StrokeOrientation.NORTH;
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
        editor.putInt(KEY_STROKEORIENTATION, orientation.getDegree()).commit();
        strokeOrientation = orientation;
    }


    private Boolean isStrokePullEnabled;

    /**
     * Checks whether <i>Wake lock</i> is enabled.
     * @return true if <i>Wake lock</i> is enabled, false otherwise.
     */
    public boolean isStrokePull() {
        if (isStrokePullEnabled == null) {
            isStrokePullEnabled = preferences.getBoolean(KEY_STROKEPULL, false);
            setStrokePull(isStrokePullEnabled);
        }

        return isStrokePullEnabled;
    }

    /**
     * Set whether to use <i>Wake lock</i>.
     * @param isEnabled true to enable, false otherwise.
     */
    public void setStrokePull(boolean isEnabled) {
        editor.putBoolean(KEY_STROKEPULL, isEnabled).commit();
        isStrokePullEnabled = isEnabled;
    }

    // Wake lock
    private Boolean isWakeLockEnabled;

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
