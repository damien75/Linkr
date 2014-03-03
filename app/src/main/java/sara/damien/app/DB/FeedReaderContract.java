package sara.damien.app.DB;

import android.provider.BaseColumns;

/**
 * Created by Sara-Fleur on 3/1/14.
 */
public class FeedReaderContract {
    public FeedReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "chat";
        public static final String COLUMN_NAME_ID1 = "ID1";
        public static final String COLUMN_NAME_ID2 = "ID2";
        public static final String COLUMN_NAME_DATE = "Date";
        public static final String COLUMN_NAME_MESSAGE = "Message";
        public static final String COLUMN_NAME_VISIBILITY = "Visibility";
    }
}
