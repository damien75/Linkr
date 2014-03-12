package sara.damien.app;

import android.os.Parcel;
import android.os.Parcelable;

public class Meeting implements Parcelable {
    private Profile otherParticipant;

    public Meeting (Profile p){
        this.otherParticipant = p;
    }

    public Profile getOtherParticipant() {
        return otherParticipant;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(otherParticipant, flags);
    }

    public static Meeting readFromParcel(Parcel parcel) {
        Meeting meeting = new Meeting((Profile) parcel.readParcelable(Profile.class.getClassLoader()));
        //meeting.otherParticipant = parcel.readParcelable(Profile.class.getClassLoader());
        return meeting;
    }

    public static final Parcelable.Creator<Meeting> CREATOR = new Parcelable.Creator<Meeting>() {
        public Meeting createFromParcel(Parcel in) { return Meeting.readFromParcel(in); }
        public Meeting[] newArray(int size) { return new Meeting[size]; }
    };
}
