package sara.damien.app.DB;

import android.provider.BaseColumns;

/**
 * Created by Damien on 03/03/2014.
 */
public class FeedMeeting {
    public FeedMeeting(){}

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "meeting";
        public static final String COLUMN_NAME_ID1 = "ID1";
        public static final String COLUMN_NAME_ID2 = "ID2";
        public static final String COLUMN_NAME_SUBJECT = "Subject";
        public static final String COLUMN_NAME_STATE = "State";
        public static final String COLUMN_NAME_MESSAGE = "Message";
        public static final String COLUMN_NAME_DATE_REQUEST = "Date_Request";
        public static final String COLUMN_NAME_DATE_ACCEPT = "Date_Accept";
        public static final String COLUMN_NAME_DATE_MEETING = "Date_Meeting";
        public static final String COLUMN_NAME_TIME = "Time";
        public static final String COLUMN_NAME_VISIBILITY = "Visibility";
    }
}
