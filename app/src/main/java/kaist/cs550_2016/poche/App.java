package kaist.cs550_2016.poche;

import android.app.Application;
import android.content.Context;

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
        ConfigHelper.init(getApplicationContext());
        App.context = getApplicationContext();
    }

    /**
     * Returns this app's context.<br>
     * !! Warning: DO NOT USE this for creating Views, Toasts, Dialogs, <br>
     *             such that stuffs bound to a single Activity.<br>
     * !! Use mainly for parsing XML, get resources, access files, etc.<br>
     * @return Application context (=getApplicationContext())
     */
    public static Context getAppContext() {
        return App.context;
    }
}