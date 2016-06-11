package kaist.cs550_2016.poche;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Leegeun Ha.
 */

/**
 * Singleton class that manages configuration stored in application.<br>
 * Configurations can be set, or retrieved through methods.<br><br>
 *
 * Developers Note: Steps to add a new configuration item<br>
 * 1. Add key item (string resource ID) in private final int.<br>
 * 2. Add an int created in step 1. to {@link ConfigHelper#KEY_ALL_CONFIGS_IN_CONFIGACTIVITY}
 *    if this is supposed to be handled for {@link ConfigActivity}.<br>
 * 3. Declare a variable to maintain configuration value, in private static [TYPE] form.<br>
 * 4. If there is no initial value for decleared variable in step 3,<br> create getter method
 *    get[VARIABLE NAME] that retrieves persistent configuration from {@link SharedPreferences}.<br>
 *    <b>IMPORTANT: Default value must be given and set if not present.</b>
 */
public class ConfigHelper {

    private final int
            KEY_PLAYORDER = R.string.pref_key_playorder,
            KEY_STROKEORIENTATION = R.string.pref_key_strokeorientation,
            KEY_STROKEPULL = R.string.pref_key_strokepull,
            KEY_WAKELOCK = R.string.pref_key_wakelock;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    /**
     * All of key lists used for {@link ConfigActivity}.
     */
    public final String[] KEY_ALL_CONFIGS_IN_CONFIGACTIVITY;

    // Load all predefined configuration keys from string.xml
    private ConfigHelper(Context context) {
        KEY_ALL_CONFIGS_IN_CONFIGACTIVITY = new String[] {
                context.getString(KEY_PLAYORDER),
                context.getString(KEY_STROKEORIENTATION),
                context.getString(KEY_STROKEPULL),
                context.getString(KEY_WAKELOCK)
        };
    }

    /**
     * Singleton instance
     */
    private static ConfigHelper ourInstance;

    /**
     * Prepare configuration from {@link SharedPreferences} and initialize Singleton instance.
     * @param context Android {@link Context} for using {@link SharedPreferences}
     */
    public static void init(Context context) {
        if (ourInstance != null) return;
        ourInstance = new ConfigHelper(context);
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
        playOrder = null;
        getPlayOrder();

        strokeOrientation = null;
        getStrokeOrientation();

        isStrokePullEnabled = null;
        isStrokePull();

        isWakeLockEnabled = null;
        isWakeLock();
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
            String modeString = preferences.getString(App.getAppString(KEY_PLAYORDER),
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
        editor.putString(App.getAppString(KEY_PLAYORDER), playOrder.toString()).commit();
        this.playOrder = playOrder;
    }

    // Stroke orientation
    public enum StrokeOrientation {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private StrokeOrientation strokeOrientation;

    /**
     * Get stroke orientation.
     * @return One of stroke orientation mode.<br>
     *          See {@link StrokeOrientation}
     */
    public StrokeOrientation getStrokeOrientation() {
        if (strokeOrientation == null) {
            String orientationString = preferences.getString(App.getAppString(KEY_STROKEORIENTATION),
                    StrokeOrientation.NORTH.toString());
            strokeOrientation = StrokeOrientation.valueOf(orientationString);
            setStrokeOrientation(strokeOrientation);
        }
        return strokeOrientation;
    }

    /**
     * Set stroke orientation.
     * @param orientation One of stroke orientation mode.<br>
     *          See {@link StrokeOrientation}
     */
    public void setStrokeOrientation(StrokeOrientation orientation) {
        editor.putString(App.getAppString(KEY_STROKEORIENTATION), orientation.toString()).commit();
        strokeOrientation = orientation;
    }

    // Pull to stroke
    private Boolean isStrokePullEnabled;

    /**
     * Checks whether <i>Pull to stroke</i> is enabled.
     * @return true if <i>Pull to stroke</i> is enabled, false otherwise.
     */
    public boolean isStrokePull() {
        if (isStrokePullEnabled == null) {
            isStrokePullEnabled = preferences.getBoolean(App.getAppString(KEY_STROKEPULL), false);
            setStrokePull(isStrokePullEnabled);
        }

        return isStrokePullEnabled;
    }

    /**
     * Set whether to use <i>Pull to stroke</i>.
     * @param isEnabled true to enable, false otherwise.
     */
    public void setStrokePull(boolean isEnabled) {
        editor.putBoolean(App.getAppString(KEY_STROKEPULL), isEnabled).commit();
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
            isWakeLockEnabled = preferences.getBoolean(App.getAppString(KEY_WAKELOCK), false);
            setWakeLock(isWakeLockEnabled);
        }

        return isWakeLockEnabled;
    }

    /**
     * Set whether to use <i>Wake lock</i>.
     * @param isEnabled true to enable, false otherwise.
     */
    public void setWakeLock(boolean isEnabled) {
        editor.putBoolean(App.getAppString(KEY_WAKELOCK), isEnabled).commit();
        isWakeLockEnabled = isEnabled;
    }
}
