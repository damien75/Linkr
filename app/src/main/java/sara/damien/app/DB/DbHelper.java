package sara.damien.app.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import sara.damien.app.Common;
import sara.damien.app.Meeting;
import sara.damien.app.Profile;
import sara.damien.app.chat.Message;

/**
 * Created by Sara-Fleur on 3/4/14.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String LOG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "localDB";

    public static final String TEXT_TYPE = " TEXT";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Profile.DB.CREATE_QUERY);
        db.execSQL(Meeting.DB.CREATE_QUERY);
        db.execSQL(Message.DB.CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //FIXME: Should we really drop the tables?
        db.execSQL("DROP TABLE IF EXISTS " + Meeting.DB.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Profile.DB.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Message.DB.NAME);
        onCreate(db);
    }

    public void insertLocalProfile (Profile profile){ //TODO: Why not store profiles as blobs, indexed by ID? It seems that all queries on profiles select only on the profile ID. This could make future evolutions simpler as well.
        getWritableDatabase().insert(Profile.DB.NAME, null, profile.serializeForLocalDB());
    }

    public boolean existIDM(String IDm){
        final String QUERY = "SELECT 1 FROM " + Meeting.DB.NAME + " WHERE " + Meeting.DB.COLUMNS.IDM + " = ?";

        return this.getReadableDatabase().rawQuery(
                QUERY,
                new String[] { IDm }
        ).getCount() > 0;
    }

    public boolean insertLocalRequestSentMeeting (Meeting request){
        boolean insertion_needed = !existIDM(request.getMeetingID());
        if (insertion_needed) {
            this.getWritableDatabase().insert(Meeting.DB.NAME, null, request.serializeForLocalDB());
        }
        return insertion_needed;
    }

    public void insertLocalDebateMeeting(Meeting meeting){ //FIXME: Duplicate of insertLocalRequestSentMeeting, unless the ID1 and ID2 were not the same
        if (!existIDM(meeting.getMeetingID())) {
            this.getWritableDatabase().insert(Meeting.DB.NAME, null, meeting.serializeForLocalDB());
        }
    }

    public ArrayList<Meeting> getRequestSentMeeting(String myID){
        final String QUERY =
            "SELECT " + Meeting.DB.COLUMNS.DATE_REQUEST + ","
                      + Meeting.DB.COLUMNS.IDM + ","
                      + Meeting.DB.COLUMNS.ID2 + ","
                      + Meeting.DB.COLUMNS.MESSAGE + ","
                      + Profile.DB.COLUMNS.FIRST_NAME + ","
                      + Profile.DB.COLUMNS.LAST_NAME + " " +
            "FROM "   + Meeting.DB.NAME + "," + Profile.DB.NAME + " " +
            "WHERE "  + Meeting.DB.COLUMNS.ID1 + "=? "
                      + "AND " + Meeting.DB.COLUMNS.STATE + "=? "
                      + "AND " + Meeting.DB.COLUMNS.ID2 + "=" + Profile.DB.COLUMNS.ID + " " +
            "ORDER BY " + Meeting.DB.COLUMNS.DATE_REQUEST + " DESC";

        Cursor c = this.getReadableDatabase().rawQuery(QUERY, new String[] {Common.getMyID(), "0"});
        c.moveToFirst();

        ArrayList<Meeting> requests = new ArrayList<Meeting>();

        while (!c.isAfterLast()){
            Log.d("rowread", String.valueOf(c.getString(0)));
            requests.add(Meeting.deserializeRequestFromLocalDB(c));
            c.moveToNext();
        }

        return requests;
    }

    private void updateSingleMeeting(Meeting meeting, ContentValues values) {
        //TODO: Is the existIDM check really necessary? Wouldn't the update just fail cleanly?
        if (existIDM(meeting.getMeetingID())) {
            this.getWritableDatabase().update(
                    Meeting.DB.NAME,
                    values,
                    Meeting.DB.COLUMNS.IDM + "=?",
                    new String[]{meeting.getMeetingID()}
            );
        }
    }

    public void updateSentRequest(Meeting request) {
        // If the request doesn't exist in the DB, it will be inserted; if it does, an update is performed.
        if (!insertLocalRequestSentMeeting(request))
            updateSingleMeeting(request, request.serializeRequestUpdateForLocalDB());
    }

    public void deleteSentRequest(String IDm) {
        this.getWritableDatabase().delete(Meeting.DB.NAME, Meeting.DB.COLUMNS.IDM, new String[]{IDm});
    }

    public void updateDateMeeting (Meeting meeting) {
        ContentValues values = meeting.serializeMeetingDateUpdateForLocalDB();

        //FIXME: Fails if the meeting is already scheduled; what do we do in that case?
        if (values != null)
            updateSingleMeeting(meeting, values);
    }

    public void updateStateMeeting(Meeting meeting) {
        updateSingleMeeting(meeting, meeting.serializeStateForLocalDB());
    }

    public void updateMeeting(Meeting meeting) {
        updateSingleMeeting(meeting, meeting.serializeUpdateForLocalDB());
    }


    public void insertMessage(Message message) { //TODO: Shouldn't DB calls be synchronized?
        this.getWritableDatabase().insert(
            Message.DB.NAME,
            null,
            message.serializeForLocalDB());
    }

    public ArrayList<Message> readAllLocalMessage(String chateeID){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Message> messages = new ArrayList<Message>();

        Cursor c = db.query(
            Message.DB.NAME,
            Message.DB.COLUMNS.PROJECTION,
            Message.DB.COLUMNS.SENDER + " = ? OR " + Message.DB.COLUMNS.RECIPIENT +" = ?",
            new String[] {chateeID , chateeID},
            null, // no row grouping
            null, // no filtering by group
            Message.DB.COLUMNS.DATE // sort column
        );

        c.moveToFirst();
        Log.d("countcursor",String.valueOf(c.getColumnCount()));

        while (!c.isAfterLast()){
            Message msg = Message.deserializeFromLocalDB(c);
            messages.add(msg);
            c.moveToNext();
        }

        return messages;
    }
}