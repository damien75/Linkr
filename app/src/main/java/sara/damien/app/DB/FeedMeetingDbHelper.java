package sara.damien.app.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import sara.damien.app.DB.FeedMeeting;

/**
 * Created by Damien on 03/03/2014.
 */
public class FeedMeetingDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedMeeting.FeedEntry.TABLE_NAME + " (" +
                    FeedMeeting.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedMeeting.FeedEntry.COLUMN_NAME_ID1 + TEXT_TYPE + COMMA_SEP +
                    FeedMeeting.FeedEntry.COLUMN_NAME_ID2 + TEXT_TYPE + COMMA_SEP +
                    FeedMeeting.FeedEntry.COLUMN_NAME_SUBJECT + TEXT_TYPE + COMMA_SEP  +
                    FeedMeeting.FeedEntry.COLUMN_NAME_STATE + TEXT_TYPE + COMMA_SEP +
                    FeedMeeting.FeedEntry.COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP  +
                    FeedMeeting.FeedEntry.COLUMN_NAME_DATE_REQUEST + TEXT_TYPE + COMMA_SEP  +
                    FeedMeeting.FeedEntry.COLUMN_NAME_DATE_ACCEPT + TEXT_TYPE + COMMA_SEP  +
                    FeedMeeting.FeedEntry.COLUMN_NAME_DATE_MEETING + TEXT_TYPE + COMMA_SEP  +
                    FeedMeeting.FeedEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP  +
                    FeedMeeting.FeedEntry.COLUMN_NAME_VISIBILITY + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedMeeting.FeedEntry.TABLE_NAME;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedMeeting.db";

    public FeedMeetingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
