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

import sara.damien.app.Common;
import sara.damien.app.Profile;
import sara.damien.app.RequestsSent;
import sara.damien.app.chat.Message;

/**
 * Created by Sara-Fleur on 3/4/14.
 */
public class DbHelper extends SQLiteOpenHelper{
    // Logcat tag
    //Les loulous :)
    //Test de fin de ligne
    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    public static final String DATABASE_NAME = "localDB";
    // Table Names
    private static final String TABLE_PROFILE = "profile";
    private static final String TABLE_MEETING = "meeting";
    private static final String TABLE_CHAT = "chat";
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

    //Chat Table - column names
    public static final String COLUMN_IDMSG = "IDmsg";
    //public static final String COLUMN_NAME_ID1 = "ID1";
    //public static final String COLUMN_NAME_ID2 = "ID2";
    public static final String COLUMN_NAME_DATE = "Date";
    //public static final String COLUMN_NAME_MESSAGE = "Message";
    //public static final String COLUMN_NAME_VISIBILITY = "Visibility";

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

    public static final String CREATE_TABLE_CHAT =
            "CREATE TABLE " + TABLE_CHAT + " (" +
                    COLUMN_IDMSG + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_ID1 + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_ID2 + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_DATE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_MESSAGE + TEXT_TYPE + COMMA_SEP  +
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
        db.execSQL(CREATE_TABLE_CHAT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEETING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT);
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

    public boolean existIDM (String IDm){
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT IDm FROM meeting WHERE IDm=?";
        String[] tabargs = {IDm};
        Cursor c = db.rawQuery(selectQuery, tabargs);
        return (c.getCount()>0);
    }

    public void insertLocalRequestSentMeeting (String IDm,String ID1, String ID2, String subject,String state,String message){
        if (!existIDM(IDm)){
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

    public void updateSentRequest (String Date_Accept,String IDm,String myID,String ID2,String Subject,String Date_Request,String message){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (existIDM(IDm)){
        values.put(COLUMN_NAME_DATE_ACCEPT,Date_Accept);
        values.put(COLUMN_NAME_STATE,"1");

        String selection = COLUMN_IDM+" = ?";
        String[] selectionArgs = new String[] {IDm};
        // updating row
        db.update(TABLE_MEETING, values, selection,
               selectionArgs);
        }
        else{
            values.put(COLUMN_IDM, IDm);
            values.put(COLUMN_NAME_ID1, myID);
            values.put(COLUMN_NAME_ID2, ID2);
            values.put(COLUMN_NAME_SUBJECT, Subject);
            values.put(COLUMN_NAME_STATE, "1");
            values.put(COLUMN_NAME_MESSAGE, message);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            values.put(COLUMN_NAME_DATE_REQUEST, Date_Request);
            values.put(COLUMN_NAME_TIME, time);
            values.put(COLUMN_NAME_DATE_ACCEPT,Date_Accept);
            db.insert(TABLE_MEETING,null,values);
        }
    }

    public void deleteSentRequest (String IDm){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = COLUMN_IDM+" = ?";
        String[] selectionArgs = new String[] {IDm};

        db.delete(TABLE_MEETING,selection,selectionArgs);

    }

    public void insertMessage (Message message) { //TODO: Shouldn't DB calls be synchronized?
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IDMSG, message.getID());
        values.put(COLUMN_NAME_DATE, message.getTime());
        values.put(COLUMN_NAME_ID1, Common.getMyID());
        values.put(COLUMN_NAME_ID2, message.getRecipient());
        values.put(COLUMN_NAME_MESSAGE, message.getMessage());
        values.put(COLUMN_NAME_VISIBILITY, "1");
        db.insert(
                TABLE_CHAT,
                null,
                values);
    }

    public ArrayList<Message> readAllLocalMessage (){ //FIXME: This doesn't filter messages.
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Message> messages = new ArrayList<Message>();
// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                COLUMN_IDMSG,
                COLUMN_NAME_MESSAGE,
                COLUMN_NAME_ID1,
                COLUMN_NAME_ID2,
                COLUMN_NAME_DATE
        };
        String sortOrder = COLUMN_NAME_DATE ;

        Cursor c = db.query(
                TABLE_CHAT,  // The table to query
                projection,                               // The columns to return
                "",                                // The columns for the WHERE clause
                null,                              // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        c.moveToFirst();
        Log.d("countcursor",String.valueOf(c.getColumnCount()));
        while (!c.isAfterLast()){
            Message msg = new Message(c.getString(c.getColumnIndex(COLUMN_IDMSG)), c.getString(c.getColumnIndex(COLUMN_NAME_MESSAGE)), c.getString(c.getColumnIndex(COLUMN_NAME_ID1)), c.getString(c.getColumnIndex(COLUMN_NAME_ID2)), c.getString(c.getColumnIndex(COLUMN_NAME_DATE)), false, true); //FIXME: Still assumes that serialized messages have indeed been sent
            messages.add(msg);
            c.moveToNext();
        }
        return messages;
    }
}