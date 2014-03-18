package sara.damien.app;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Meeting implements Parcelable {
    private Profile otherParticipant;
    private String meetingID;
    private String subject;
    private String state;
    private String myStatus;
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
        params.add(new BasicNameValuePair("myStatus",this.myStatus));
        return params;
    }
}
