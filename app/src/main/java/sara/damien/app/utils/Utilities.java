package sara.damien.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by clement on 08/03/14.
 */
public class Utilities {
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getTimestamp() {
        return DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    public static <T> String join(final Iterable<T> seq, final String separator) {
        StringBuilder sb = new StringBuilder(256);

        int count = 0;
        for (T elem : seq) {
            if (count > 0)
                sb.append(separator);
            sb.append(elem.toString());
            count++;
        }

        return sb.toString();
    }
}
