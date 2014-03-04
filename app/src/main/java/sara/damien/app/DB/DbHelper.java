package sara.damien.app.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import sara.damien.app.Profile;
import sara.damien.app.RequestsSent;

/**
 * Created by Sara-Fleur on 3/4/14.
 */
public class DbHelper extends SQLiteOpenHelper{

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    public static final String DATABASE_NAME = "localDB";

    // Table Names
    private static final String TABLE_PROFILE = "profile";
    private static final String TABLE_MEETING = "meeting";

    // Profile column names
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME_LAST_NAME = "Last_Name";
    public static final String COLUMN_NAME_FIRST_NAME = "First_Name";
    public static final String COLUMN_NAME_LAST_SUBJECT = "Last_Subject";
    public static final String COLUMN_NAME_LOC_X = "Loc_X";
    public static final String COLUMN_NAME_LOC_Y = "Loc_Y";
    public static final String COLUMN_NAME_COMPANY = "Company";
    public static final String COLUMN_NAME_EXP_YEARS = "Exp_Years";
    public static final String COLUMN_NAME_SUM_GRADE = "Sum_Grade";
    public static final String COLUMN_NAME_NUMBER_GRADE = "Number_Grade";
    public static final String COLUMN_NAME_PICTURE = "Picture";

    // Meeting Table - column names
    public static final String COLUMN_IDM = "IDm";
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

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    // Table Create Statements
    // Profile table create statement
    private static final String CREATE_TABLE_PROFILE =
            "CREATE TABLE " + TABLE_PROFILE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LAST_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LAST_SUBJECT + TEXT_TYPE + COMMA_SEP  +
                    COLUMN_NAME_LOC_X + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_LOC_Y + TEXT_TYPE + COMMA_SEP  +
                    COLUMN_NAME_COMPANY + TEXT_TYPE + COMMA_SEP  +
                    COLUMN_NAME_EXP_YEARS + TEXT_TYPE + COMMA_SEP  +
                    COLUMN_NAME_SUM_GRADE + TEXT_TYPE + COMMA_SEP  +
                    COLUMN_NAME_NUMBER_GRADE + TEXT_TYPE + COMMA_SEP  +
                    COLUMN_NAME_PICTURE + TEXT_TYPE +
                    " )";

    // Meeting table create statement
    private static final String CREATE_TABLE_MEETING =
            "CREATE TABLE " + TABLE_MEETING + " (" +
            COLUMN_IDM + " INTEGER PRIMARY KEY," +
            COLUMN_NAME_ID1 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_ID2 + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_SUBJECT + TEXT_TYPE + COMMA_SEP  +
            COLUMN_NAME_STATE + TEXT_TYPE + COMMA_SEP +
            COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP  +
            COLUMN_NAME_DATE_REQUEST + TEXT_TYPE + COMMA_SEP  +
            COLUMN_NAME_DATE_ACCEPT + TEXT_TYPE + COMMA_SEP  +
            COLUMN_NAME_DATE_MEETING + TEXT_TYPE + COMMA_SEP  +
            COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP  +
            COLUMN_NAME_VISIBILITY + TEXT_TYPE +
            " )";


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(CREATE_TABLE_PROFILE);
        db.execSQL(CREATE_TABLE_MEETING);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEETING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        // create new tables
        onCreate(db);
    }

    public void insertLocalProfile (Profile profile){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID,profile.getID());
        values.put(COLUMN_NAME_FIRST_NAME, profile.getFirst_Name());
        values.put(COLUMN_NAME_LAST_NAME, profile.getLast_Name());
        values.put(COLUMN_NAME_LAST_SUBJECT, profile.getLast_Subject());
        values.put(COLUMN_NAME_LOC_X, profile.getLoc_X());
        values.put(COLUMN_NAME_LOC_Y, profile.getLoc_Y());
        values.put(COLUMN_NAME_COMPANY, profile.getCompany());
        values.put(COLUMN_NAME_EXP_YEARS, profile.getExp_Years());
        values.put(COLUMN_NAME_SUM_GRADE, profile.getSum_Grade());
        values.put(COLUMN_NAME_NUMBER_GRADE, profile.getNumber_Grade());

        db.insert(TABLE_PROFILE, null, values);
    }

    public void insertLocalRequestSentMeeting (String IDm,String ID1, String ID2, String subject,String state,String message){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IDM, IDm);
        values.put(COLUMN_NAME_ID1, ID1);
        values.put(COLUMN_NAME_ID2, ID2);
        values.put(COLUMN_NAME_SUBJECT, subject);
        values.put(COLUMN_NAME_STATE, state);
        values.put(COLUMN_NAME_MESSAGE, message);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
        values.put(COLUMN_NAME_DATE_REQUEST, time);
        values.put(COLUMN_NAME_TIME, time);
        db.insert(TABLE_MEETING,null,values);
    }

    public ArrayList<RequestsSent> getRequestSentMeeting(String myID){
        ArrayList<RequestsSent> request = new ArrayList<RequestsSent>();

        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT Date_Request,IDm,ID2,Subject,Message,First_Name,Last_Name FROM meeting,profile WHERE ID1=? AND State=? AND ID2=ID ORDER BY Date_Request DESC";
        String[] tabargs = {myID,"0"};
        Cursor c = db.rawQuery(selectQuery, tabargs);
        c.moveToFirst();
        while (!c.isAfterLast()){
            Log.d("rowread", String.valueOf(c.getString(0)));
            request.add(new RequestsSent(c.getString(0), c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6)));
            c.moveToNext();
        }
        return request;
    }

    public void updateSentRequest (String Date_Accept,String IDm){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_DATE_ACCEPT,Date_Accept);
        values.put(COLUMN_NAME_STATE,"1");

        String selection = COLUMN_IDM+" = ?";
        String[] selectionArgs = new String[] {IDm};
        // updating row
        db.update(TABLE_MEETING, values, selection,
               selectionArgs);
    }

    public void deleteSentRequest (String IDm){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_IDM+" = ?";
        String[] selectionArgs = new String[] {IDm};

        db.delete(TABLE_MEETING,selection,selectionArgs);

    }
}
