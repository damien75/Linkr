package sara.damien.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Damien on 01/03/2014.
 */
public class FeedProfileDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedProfile.FeedEntry.TABLE_NAME + " (" +
                    FeedProfile.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedProfile.FeedEntry.COLUMN_NAME_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedProfile.FeedEntry.COLUMN_NAME_LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedProfile.FeedEntry.COLUMN_NAME_LAST_SUBJECT + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COLUMN_NAME_LOC_X + TEXT_TYPE + COMMA_SEP +
                    FeedProfile.FeedEntry.COLUMN_NAME_LOC_Y + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COLUMN_NAME_COMPANY + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COLUMN_NAME_EXP_YEARS + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COLUMN_NAME_SUM_GRADE + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COLUMN_NAME_NUMBER_GRADE + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COLUMN_NAME_STATE + TEXT_TYPE + COMMA_SEP  +
                    FeedProfile.FeedEntry.COMUMN_NAME_PICTURE + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedProfile.FeedEntry.TABLE_NAME;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "FeedProfile.db";

    public FeedProfileDbHelper(Context context) {
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
