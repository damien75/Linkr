package sara.damien.app;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import sara.damien.app.DB.DbHelper;

/**
 * Created by clement on 08/03/14.
 */
public class Common {
    private static DbHelper DB;
    private static LinkrPreferences prefs;
    private static boolean is_debugging;
    private static LinkrCalendar calendar;

    public static DbHelper getDB() {
        return DB;
    }

    public static LinkrPreferences getPrefs() { // FIXME: Use everywhere
        return prefs;
    }

    public static LinkrCalendar getCalendar(){return calendar;}

    public static boolean isDebugging() { return is_debugging; }

    public static String getMyID() { // FIXME: Use everywhere
        return prefs.getID(); //TODO: Fail when offline?
    }

    public static void Init(Context appContext) {
        DB = new DbHelper(appContext);
        prefs = new LinkrPreferences(appContext);
        calendar = new LinkrCalendar(appContext);
        is_debugging = android.os.Debug.isDebuggerConnected();
    }

    public static String setDateToLocalTimezone(String dateFromServer) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateFromServer);
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone timeZone = TimeZone.getDefault();
        int offset = timeZone.getOffset(System.currentTimeMillis());
        calendar.setTime(date);
        calendar.add(Calendar.MILLISECOND,offset);
        return format.format(calendar.getTime());
    }
}
