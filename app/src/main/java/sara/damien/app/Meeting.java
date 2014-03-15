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

    public Meeting (Profile otherParticipant, String meetingID, String subject, String state, String myStatus, String dateAccept, String dateRequest, String dateMeeting){
        this.otherParticipant = otherParticipant;
        this.meetingID = meetingID;
        this.subject = subject;
        this.state = state;
        this.myStatus = myStatus;
        this.dateAccept = dateAccept;
        this.dateRequest = dateRequest;
        this.dateMeeting = dateMeeting;
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
    public void setDateMeeting(String dateMeeting) { this.dateMeeting = dateMeeting;}

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
