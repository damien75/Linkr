package sara.damien.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import sara.damien.app.DB.DbHelper;

/**
 * Created by clement on 08/03/14.
 */
public class Common {
    private static DbHelper DB;
    private static SharedPreferences prefs;
    private static boolean is_debugging;

    public static DbHelper getDB() {
        return DB;
    }

    public static SharedPreferences getPrefs() { // FIXME: Use everywhere
        return prefs;
    }

    public static boolean isDebugging() { return is_debugging; }

    public static String getMyID() { // FIXME: Use everywhere
        return prefs.getString("ID", "0"); //TODO: Fail when offline?
    }

    public static void Init(Context appContext) {
        DB = new DbHelper(appContext);
        prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        is_debugging = android.os.Debug.isDebuggerConnected();
    }
}
