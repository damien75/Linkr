package sara.damien.app;

import android.content.Context;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.utils.Utilities;

/**
 * Created by clement on 08/03/14.
 */
public class Common {
    private static Context appContext;

    private static DbHelper DB;
    private static LinkrPreferences prefs;
    private static boolean is_debugging;
    private static LinkrCalendar calendar;

    public static void Init(Context appContext) {
        Common.appContext = appContext;
        Utilities.Init();

        DB = new DbHelper(appContext);
        prefs = new LinkrPreferences(appContext);
        calendar = new LinkrCalendar(appContext);
        is_debugging = android.os.Debug.isDebuggerConnected();
    }

    public static Context getAppContext() { // FIXME: Use everywhere
        return appContext;
    }

    public static DbHelper getDB() {
        return DB;
    }

    public static LinkrPreferences getPrefs() { // FIXME: Use everywhere
        return prefs;
    }

    public static LinkrCalendar getCalendar(){ return calendar; }

    public static boolean isDebugging() { return is_debugging; }

    public static String getMyID() { // FIXME: Use everywhere
        return prefs.getID(); //TODO: Fail when offline?
    }
}
