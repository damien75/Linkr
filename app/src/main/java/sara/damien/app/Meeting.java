package sara.damien.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.utils.Utilities;

public class Meeting implements Parcelable {
    private Profile otherParticipant;
    private String meetingID;
    private String subject;
    private String state;
    private String myStatus; //FIXME: States should use an enum
    //TODO : passer de String à Date (en utilisant timezone)
    private String dateAccept;
    private String dateRequest;
    private String dateMeeting;
    private String message;
    private long calendarEventID;

    public Meeting (Profile otherParticipant, String meetingID, String subject, String state, String myStatus, String dateAccept, String dateRequest, String dateMeeting,String message){
        this.otherParticipant = otherParticipant;
        this.meetingID = meetingID;
        this.subject = subject;
        this.state = state;
        this.myStatus = myStatus;
        this.dateAccept = dateAccept;
        this.dateRequest = dateRequest;
        this.dateMeeting = dateMeeting;
        this.message = message;
    }

    //Constructor for requests_sent
    public Meeting (String Date_Request,String meetingID, String ID2, String Subject, String Message, String First_Name, String Last_Name){
        this.dateRequest = Date_Request;
        this.meetingID = meetingID;
        this.otherParticipant =  new Profile(ID2,First_Name,Last_Name);
        this.subject = Subject;
        this.message = Message;

    }

    public Meeting (){
    }

    public Profile getOtherParticipant () {
        return otherParticipant;
    }
    public String getMeetingID () { return meetingID; }
    public String getState (){ return state; }
    public String getSubject () { return this.subject; }
    public String getMyStatus () { return this.myStatus; }
    public String getDateAccept () { return this.dateAccept; }
    public String getDateRequest () { return this.dateRequest; }
    public String getDateMeeting () { return  this.dateMeeting; }
    public long getCalendarEventID(){return this.calendarEventID;}
    public String getMessage(){return this.message;}
    public void setDateMeeting(String dateMeeting) { this.dateMeeting = dateMeeting;}
    public void setState(String state){ this.state = state;}
    public void setCalendarEventID (long eventID){this.calendarEventID = eventID;}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(otherParticipant, flags);
        parcel.writeString(meetingID);
        parcel.writeString(state);
        parcel.writeString(subject);
        parcel.writeString(myStatus);
        parcel.writeString(dateAccept);
        parcel.writeString(dateRequest);
        parcel.writeString(dateMeeting);
        parcel.writeString(message);
    }

    public static Meeting readFromParcel(Parcel parcel) {
        Meeting meeting = new Meeting();
        meeting.otherParticipant = parcel.readParcelable(Profile.class.getClassLoader());
        meeting.meetingID = parcel.readString();
        meeting.state = parcel.readString();
        meeting.subject = parcel.readString();
        meeting.myStatus = parcel.readString();
        meeting.dateAccept = parcel.readString();
        meeting.dateRequest = parcel.readString();
        meeting.dateMeeting = parcel.readString();
        meeting.message = parcel.readString();
        return meeting;
    }

    public static final Parcelable.Creator<Meeting> CREATOR = new Parcelable.Creator<Meeting>() {
        public Meeting createFromParcel(Parcel in) { return Meeting.readFromParcel(in); }
        public Meeting[] newArray(int size) { return new Meeting[size]; }
    };

    public List<NameValuePair> serializeForLinkr() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("meetingID", this.meetingID));
        params.add(new BasicNameValuePair("dateMeeting", this.dateMeeting));
        params.add(new BasicNameValuePair("myStatus", this.myStatus));
        return params;
    }

    public static interface DB {
        public static interface COLUMNS {
            String IDM = "IDm";
            String ID1 = "ID1";
            String ID2 = "ID2";
            String SUBJECT = "Subject";
            String STATE = "State";
            String MESSAGE = "Message";
            String DATE_REQUEST = "Date_Request";
            String DATE_ACCEPT = "Date_Accept";
            String DATE_MEETING = "Date_Meeting";
            String TIME = "Time";
            String VISIBILITY = "Visibility";
            String CALENDAR_EVENTID = "Calendar_EventID";
        }

        String NAME = "meeting";

        String CREATE_QUERY =
                "CREATE TABLE " + NAME + " (" +
                        COLUMNS.IDM + " INTEGER PRIMARY KEY," +
                        COLUMNS.ID1 + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.ID2 + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.SUBJECT + DbHelper.TEXT_TYPE + ","  +
                        COLUMNS.STATE + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.MESSAGE + DbHelper.TEXT_TYPE + ","  +
                        COLUMNS.DATE_REQUEST + DbHelper.TEXT_TYPE + ","  +
                        COLUMNS.DATE_ACCEPT + DbHelper.TEXT_TYPE + ","  +
                        COLUMNS.DATE_MEETING + DbHelper.TEXT_TYPE + ","  +
                        COLUMNS.TIME + DbHelper.TEXT_TYPE + ","  +
                        COLUMNS.VISIBILITY + DbHelper.TEXT_TYPE + "," +
                        COLUMNS.CALENDAR_EVENTID + DbHelper.TEXT_TYPE +
                        " )";
    }

    public static Meeting deserializeRequestFromLocalDB(Cursor c) {
        Meeting request = new Meeting();

        Object a = "SELECT " + Meeting.DB.COLUMNS.DATE_REQUEST + ","
                + Meeting.DB.COLUMNS.IDM + ","
                + Meeting.DB.COLUMNS.ID2 + ","
                + Meeting.DB.COLUMNS.MESSAGE + ","
                + Profile.DB.COLUMNS.FIRST_NAME + ","
                + Profile.DB.COLUMNS.LAST_NAME + " ";

        request.dateRequest = c.getString(c.getColumnIndex(DB.COLUMNS.DATE_REQUEST));
        request.meetingID = c.getString(c.getColumnIndex(DB.COLUMNS.IDM));
        request.message = c.getString(c.getColumnIndex(DB.COLUMNS.MESSAGE));
        request.otherParticipant = new Profile(
                c.getString(c.getColumnIndex(DB.COLUMNS.ID2)),
                c.getString(c.getColumnIndex(Profile.DB.COLUMNS.FIRST_NAME)),
                c.getString(c.getColumnIndex(Profile.DB.COLUMNS.LAST_NAME))
        );

        return request;
    }

    //FIXME: The name implies that the whole thing is serialized, while it's only the initial values
    public ContentValues serializeForLocalDB() {
        // FIXME: That's fishy; why isn't the timeStamp already
        // stored in dateRequest and time?

        String timeStamp = Utilities.getTimestamp();

        ContentValues values = new ContentValues();
        values.put(DB.COLUMNS.IDM, meetingID);
        values.put(DB.COLUMNS.ID1, Common.getMyID()); //FIXME: Check
        values.put(DB.COLUMNS.ID2, otherParticipant.getID()); //FIXME: Check
        values.put(DB.COLUMNS.SUBJECT, subject);
        values.put(DB.COLUMNS.STATE, state);
        values.put(DB.COLUMNS.MESSAGE, message);
        values.put(DB.COLUMNS.DATE_REQUEST, timeStamp);
        values.put(DB.COLUMNS.TIME, timeStamp);

        return values;
    }

    public ContentValues serializeUpdateForLocalDB() {
        ContentValues values = new ContentValues();

        values.put(DB.COLUMNS.STATE, state);
        values.put(DB.COLUMNS.SUBJECT, subject);
        values.put(DB.COLUMNS.MESSAGE, message);
        values.put(DB.COLUMNS.DATE_REQUEST, dateRequest);
        values.put(DB.COLUMNS.DATE_ACCEPT, dateAccept);
        values.put(DB.COLUMNS.DATE_MEETING, dateMeeting);
        values.put(DB.COLUMNS.CALENDAR_EVENTID, calendarEventID);

        return values;
    }

    //FIXME: Find better names for these two functions.

    public ContentValues serializeStateForLocalDB() {
        ContentValues values = new ContentValues();
        values.put(DB.COLUMNS.STATE, state);
        return values;
    }

    public ContentValues serializeRequestUpdateForLocalDB() {
        ContentValues values = new ContentValues();
        values.put(DB.COLUMNS.DATE_ACCEPT, dateAccept);
        values.put(DB.COLUMNS.STATE, "1");
        return values;
    }

    public ContentValues serializeMeetingDateUpdateForLocalDB() {
        // FIXME: Moving to regular dates should remove the need for the length>0 check; dateMeeting != null should be an assert
        if (dateMeeting != null && dateMeeting.length() > 0) {
            ContentValues values = new ContentValues();
            values.put(DB.COLUMNS.DATE_MEETING, dateMeeting);
            // FIXME: This looks really fishy: why aren't we storing state directly, instead of
            // computing something from myStatus?
            values.put(DB.COLUMNS.STATE, myStatus.equals("1") ? "3" : "4");
            return values;
        } else {
            return null;
        }
    }
}
