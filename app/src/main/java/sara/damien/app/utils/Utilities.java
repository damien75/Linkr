package sara.damien.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by clement on 08/03/14.
 */
public class Utilities {
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String API_URL = "http://www.golinkr.net"; //FIXME: Use this everywhere.

    public static String getTimestamp() {
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }
}
