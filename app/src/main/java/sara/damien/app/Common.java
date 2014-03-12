package sara.damien.app;

import android.content.Context;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.LinkrPreferences;

/**
 * Created by clement on 08/03/14.
 */
public class Common {
    private static DbHelper DB;
    private static LinkrPreferences prefs;
    private static boolean is_debugging;

    public static DbHelper getDB() {
        return DB;
    }

    public static LinkrPreferences getPrefs() { // FIXME: Use everywhere
        return prefs;
    }

    public static boolean isDebugging() { return is_debugging; }

    public static String getMyID() { // FIXME: Use everywhere
        return prefs.getID(); //TODO: Fail when offline?
    }

    public static void Init(Context appContext) {
        DB = new DbHelper(appContext);
        prefs = new LinkrPreferences(appContext);
        is_debugging = android.os.Debug.isDebuggerConnected();
    }
}
