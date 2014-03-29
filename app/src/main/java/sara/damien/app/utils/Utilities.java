package sara.damien.app.utils;

import android.widget.DatePicker;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by clement on 08/03/14.
 */
public class Utilities {
    private static DateFormat SERIAL_DATE_FORMAT;
    private static DateFormat SIMPLE_DATE_FORMAT;

    public static void Init() {
        SIMPLE_DATE_FORMAT = SimpleDateFormat.getInstance();
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getDefault());

        SERIAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SERIAL_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String getTimestamp() {
        return SERIAL_DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    public static String serializeDateTime(Calendar date) {
        return SERIAL_DATE_FORMAT.format(date);
    }

    public static String formatDateTime(Calendar date) {
        return SIMPLE_DATE_FORMAT.format(date);
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

    public static int wrapIndex(int index, int size) {
        return ((index % size) + size) % size;
    }

    public static <T> T fallback(T value, T fallbackValue) {
        return value != null ? value : fallbackValue;
    }

    public static Calendar calendarFromDateAndTimePickers(DatePicker date_picker, TimePicker time_picker) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(date_picker.getYear(), date_picker.getMonth(), date_picker.getDayOfMonth(), time_picker.getCurrentHour(), time_picker.getCurrentMinute());
        return calendar;
    }

    public static String deserializeAndConvertToLocalTime(String dateString) {
        try {
            Date date = SERIAL_DATE_FORMAT.parse(dateString);
            return SIMPLE_DATE_FORMAT.format(date);
        } catch (ParseException e) {
           return "[invalid date]";
        }
    }
}
