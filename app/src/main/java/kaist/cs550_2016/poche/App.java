package kaist.cs550_2016.poche;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by Leegeun Ha.
 */

/**
 * Provides overall lifecycle managed by entire application.
 */
public class App extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        App.context = getApplicationContext();
        ConfigHelper.init(getApplicationContext());
    }

    /**
     * Returns this app's context.<br>
     * !! Warning: DO NOT USE this for creating Views, Dialogs, <br>
     *             such that stuffs bound to a single Activity.<br>
     * !! Use mainly for parsing XML, get resources, access files, etc.<br>
     * @return Application context (=getApplicationContext())
     */
    public static Context getAppContext() {
        return App.context;
    }

    /**
     * Returns this app's resource.
     * @return Application resource (=getResources())
     */
    public static Resources getAppResources() {
        return context.getResources();
    }

    /**
     * Returns this app's string resource, given string ID.
     * @param id ID of string resource. See {@link kaist.cs550_2016.poche.R.string}
     * @return String defined in string resources (=getString())
     */
    public static String getAppString(int id) {
        return getAppResources().getString(id);
    }
}