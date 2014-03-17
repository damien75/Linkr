package sara.damien.app;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import sara.damien.app.utils.ImageDownloader;

/**
 * Created by Damien on 17/02/2014.
 */
public class Profile implements Parcelable {
    private boolean downloaded;
    private String Last_Name; //TODO: rename
    private String First_Name;
    private double Loc_X;
    private double Loc_Y;
    private String Last_Subject;
    private String Company;
    private int Sum_Grade;
    private int Number_Grade;

    private String ID, linkedInID;

    private String headline;

    private ProfileListener parent;
    private Bitmap picture;
    private boolean pictureDownloaded;
    private int yearsOfExperience;
    private String pictureURL;
    private String industry;
    private int state; //TODO: replace with an ENUM

    private Profile() {
        downloaded = false;
        pictureDownloaded = false;
    }

    //TODO: Does this comment make sense? "ONLY FOR SINGLE PROFILE ACTIVITIES"
    public Profile (String ID){
        this.ID = ID;
    }

    public Profile (String ID, ProfileListener parent){ //TODO: Is this useful?
        this.ID = ID;
        this.parent = parent;
    }

    public Profile(String ID, String First_Name, String Last_Name){
        this.ID = ID;
        this.First_Name = First_Name;
        this.Last_Name = Last_Name;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLinkedInID() {
        return linkedInID;
    }

    public String getProfilePictureURL() {
        return pictureURL;
    }
    public Profile (boolean downloaded,String last_Name,String first_name,String last_subject, int yearsOfExperience, double loc_X, double loc_Y, String company, String ID, int sum_Grade, int number_Grade){
        this.downloaded=downloaded;
        this.Last_Name=last_Name;
        this.Company=company;
        this.First_Name=first_name;
        this.Last_Subject=last_subject;
        this.yearsOfExperience=yearsOfExperience;
        this.Loc_X=loc_X;
        this.Loc_Y=loc_Y;
        this.Sum_Grade=sum_Grade;
        this.ID=ID;
        this.Number_Grade=number_Grade;
    }

    public boolean isDownloaded (){
        return this.downloaded;
    }

    public String getFirst_Name(){
        return this.First_Name;
    }

    public String getLast_Name(){
        return this.Last_Name;
    }

    public String getName() {
        if (First_Name != null && Last_Name != null)
            return First_Name + " " + (Common.isDebugging() ? (Last_Name + " - " + ID) : Last_Name);
        else
            return null;
    }

    public String get_Avg_Grade(){
        double avg = (double)this.Sum_Grade/((double)this.Number_Grade);
        return String.valueOf(avg);
    }
    public String getCompany (){return this.Company;}
    public String getYearsOfExperience (){return String.valueOf(this.yearsOfExperience);}
    public String getID (){return this.ID;}
    public String getLast_Subject (){return this.Last_Subject;}
    public Bitmap getPicture(){return picture;}
    public double getLoc_X(){return Loc_X;}
    public double getLoc_Y(){return Loc_Y;}
    public int getSum_Grade(){return this.Sum_Grade;}
    public int getNumber_Grade(){return this.Number_Grade;}
    public String getIndustry() {
        return industry;
    }
    public String getHeadline() {
        return headline;
    }

    public void onImageReceived(Bitmap picture) {
        this.picture = picture;
        if (parent != null)
            parent.onPictureUpdated();
    }

    public void downloadPicture() { //TODO: Sync issues
        if (!pictureDownloaded) {
            pictureDownloaded = true;
            new ImageDownloader(this).execute(this.ID);
        }
    }

    public void deletePicture() { //FIXME: Implement an image cache
        if (picture != null) {
            pictureDownloaded = false;
            picture = null;
        }
    }

    public static Profile readFromLinkedInJSON(String JSONProfileInfo) {
        Profile profile = new Profile();

        try {
            JSONObject jsonObject = new JSONObject(JSONProfileInfo);

            profile.First_Name = jsonObject.getString("firstName");
            profile.Last_Name = jsonObject.getString("lastName");
            profile.headline = jsonObject.getString("headline");

            JSONArray pictures = jsonObject.getJSONObject("pictureUrls").getJSONArray("values"); //TODO: Check (does "values" always exist?
            if (pictures.length() > 0)
                profile.pictureURL = pictures.get(0).toString();

            JSONObject positions = jsonObject.getJSONObject("positions");

            int nb_positions = positions.getInt("_total");
            JSONArray positions_list = positions.getJSONArray("values"); //TODO: Check (same as above)

            int first_position = Calendar.getInstance().get(Calendar.YEAR);

            for (int id_position = 0; id_position < nb_positions; id_position++) { //LATER: Isn't there a way to get linkedin to return a sorted list?
                int position = positions_list.getJSONObject(id_position).getJSONObject("startDate").getInt("year");

                if (position < first_position) {
                    first_position = position;
                }
            }

            profile.yearsOfExperience = Calendar.getInstance().get(Calendar.YEAR) - first_position;
            profile.linkedInID = jsonObject.getString("id");
            //TODO: profile.countryCode = jsonObject.getJSONObject("country").getString("code");
            //TODO: profile.origin = jsonObject.getString("name");
            profile.industry = jsonObject.getString("industry");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return profile;
    }

    public static Profile createMockProfile() {
        Profile profile = new Profile();
        Random prng = new Random();

        profile.downloaded = true;
        profile.ID = Integer.toString(prng.nextInt());
        profile.First_Name = "first";
        profile.Last_Name = "last";
        profile.Company = "company";
        profile.industry = "industry";
        profile.yearsOfExperience = prng.nextInt();
        profile.headline = "lorem ipsum dolor sit amet"; //TODO
        profile.Sum_Grade = 0; //TODO
        profile.Number_Grade = 0;

        return profile;
    }

    public void writeToParcel(Parcel out, int flags) {
        // FIXME: the picture field is not serialized; it should probably stored in an app-wide
        // image cache instead, from which images would be retrieved and that would  properly
        // handle memory scarcity issues.
        out.writeByte(downloaded ? (byte) 1 : 0);
        out.writeString(Last_Name);
        out.writeString(First_Name);
        out.writeDouble(Loc_X);
        out.writeDouble(Loc_Y);
        out.writeString(Last_Subject);
        out.writeString(Company);
        out.writeInt(Sum_Grade);
        out.writeInt(Number_Grade);
        out.writeString(ID);
        out.writeString(linkedInID);
        out.writeString(headline);
        out.writeInt(yearsOfExperience);
        out.writeString(pictureURL);
        out.writeString(industry);
    }

    public static Profile readFromParcel(Parcel parcel) {
        Profile profile = new Profile();

        profile.downloaded = parcel.readByte() !=0;
        profile.Last_Name = parcel.readString();
        profile.First_Name = parcel.readString();
        profile.Loc_X = parcel.readDouble();
        profile.Loc_Y = parcel.readDouble();
        profile.Last_Subject = parcel.readString();
        profile.Company = parcel.readString();
        profile.Sum_Grade = parcel.readInt();
        profile.Number_Grade = parcel.readInt();
        profile.ID = parcel.readString();
        profile.linkedInID = parcel.readString();
        profile.headline = parcel.readString();
        profile.yearsOfExperience = parcel.readInt();
        profile.pictureURL = parcel.readString();
        profile.industry = parcel.readString();

        return profile;
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel in) { return Profile.readFromParcel(in); }
        public Profile[] newArray(int size) { return new Profile[size]; }
    };

    public void setFromLinkrJSON(JSONObject json) {
        try {
            this.downloaded = true;
            //FIXME Missing: ID, linkedInID, headline, pictureURL, industry;
            this.Last_Name = json.getString("Last_Name");
            this.First_Name = json.getString("First_Name");
            this.Loc_X = json.getDouble("Loc_X");
            this.Loc_Y = json.getDouble("Loc_Y");
            this.Last_Subject = json.getString("Last_Subject");
            this.Company = json.getString("Company");
            this.Sum_Grade = json.getInt("Sum_Grade");
            this.Number_Grade = json.getInt("Number_Grade");
            this.yearsOfExperience = json.getInt("Exp_Years");
        } catch (JSONException e) { //FIXME
            e.printStackTrace();
        }

        if (parent != null)
            parent.onContentsDownloaded();
    }

    public List<NameValuePair> serializeForLinkr() { //TODO: Use JSON as well?
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("IDL", linkedInID));
        params.add(new BasicNameValuePair("Last_Name", Last_Name));
        params.add(new BasicNameValuePair("First_Name", First_Name));
        params.add(new BasicNameValuePair("Company", industry));
        params.add(new BasicNameValuePair("yearsOfExperience", String.valueOf(yearsOfExperience)));
        params.add(new BasicNameValuePair("Picture", pictureURL));
        return params;
    }

    public void setState(int state) { //TODO: Implement using an Enum
        this.state = state;
    }

    public int getState() {
        return state;
    }

    @Override
    public String toString() {
        return "Profile { " + getName() +  " }";
    }

    @Override
    public int hashCode() {
        return (ID == null ? 0 : ID.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (ID == null || !(o instanceof Profile))
            return false;

        Profile other = (Profile)o;
        return ID.equals(other.ID);
    }

    public void setParent(ProfileListener parent) {
        this.parent = parent;
    }
}
