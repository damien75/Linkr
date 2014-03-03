package sara.damien.app;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import sara.damien.app.utils.ImageDownloader;

/**
 * Created by Damien on 17/02/2014.
 */
public class Profile implements Parcelable {
    private boolean  downloaded;
    private String Last_Name;
    private String First_Name;
    private double Loc_X;
    private double Loc_Y;
    private String Last_Subject;
    private String Company;
    private int Exp_Years;
    private int Sum_Grade;
    private int Number_Grade;
    private String ID;
    private int State;

    private DefinitiveProfileActivity parent;
    private Bitmap picture;
    private boolean pictureDownloaded;

    //ONLY FOR SINGLE PROFILE ACTIVITIES
    public Profile (String ID){this.ID = ID;}

    public Profile (String ID, DefinitiveProfileActivity parent){
        this.ID = ID;
        this.parent = parent;
    }

    public void setProfileFromJson (JSONObject json){
        try {
            this.Last_Name=json.getString("Last_Name");
            this.First_Name=json.getString("First_Name");
            this.Company=json.getString("Company");
            this.Last_Subject=json.getString("Last_Subject");
            this.Exp_Years=json.getInt("Exp_Years");
            this.Loc_X=json.getDouble("Loc_X");
            this.Loc_Y=json.getDouble("Loc_Y");
            this.Sum_Grade=json.getInt("Sum_Grade");
            this.Number_Grade=json.getInt("Number_Grade");
            this.State=0;
            this.downloaded=true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Profile (boolean downloaded,String last_Name,String first_name,String last_subject, int exp_Years, double loc_X, double loc_Y, String company, String ID, int sum_Grade, int number_Grade,int State){
        this.downloaded=downloaded;
        this.Last_Name=last_Name;
        this.Company=company;
        this.First_Name=first_name;
        this.Last_Subject=last_subject;
        this.Exp_Years=exp_Years;
        this.Loc_X=loc_X;
        this.Loc_Y=loc_Y;
        this.Sum_Grade=sum_Grade;
        this.ID=ID;
        this.Number_Grade=number_Grade;
        this.State=State;
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
    public String get_Avg_Grade(){
        double avg = (double)this.Sum_Grade/((double)this.Number_Grade);
        return String.valueOf(avg);
    }
    public String getCompany (){return this.Company;}
    public String getExp_Years (){return String.valueOf(this.Exp_Years);}
    public String getID (){return this.ID;}
    public String getLast_Subject (){return this.Last_Subject;}
    public int getState (){return this.State;}
    public Bitmap getPicture(){return picture;}
    public double getLoc_X(){return Loc_X;}
    public double getLoc_Y(){return Loc_Y;}
    public int getSum_Grade(){return this.Sum_Grade;}
    public int getNumber_Grade(){return this.Number_Grade;}

    public void setState (int i){this.State=i;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(ID);
        parcel.writeString(Last_Name);
        parcel.writeString(First_Name);
        parcel.writeString(Company);
        parcel.writeString(Last_Subject);
        parcel.writeInt(Exp_Years);
        parcel.writeDouble(Loc_X);
        parcel.writeDouble(Loc_Y);
        parcel.writeInt(Sum_Grade);
        parcel.writeInt(Number_Grade);
    }
    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>()
    {
        @Override
        public Profile createFromParcel(Parcel source)
        {
            return new Profile(source);
        }

        @Override
        public Profile[] newArray(int size)
        {
            return new Profile[size];
        }
    };

    public Profile(Parcel in) {
        this.ID = in.readString();
        this.Last_Name = in.readString();
        this.First_Name = in.readString();
        this.Company = in.readString();
        this.Last_Subject = in.readString();
        this.Exp_Years = in.readInt();
        this.Loc_X = in.readDouble();
        this.Loc_Y = in.readDouble();
        this.Sum_Grade = in.readInt();
        this.Number_Grade = in.readInt();
    }

    public void onImageReceived(Bitmap picture) {
        this.picture = picture;
        parent.update(parent.currentpos);
    }

    public void downloadPicture() {
        if (!pictureDownloaded) {
            pictureDownloaded = true;
            new ImageDownloader(this).execute(this.ID);
        }
    }

    public void deletePicture() {
        if (picture != null) {
            pictureDownloaded = false;
            picture = null;
        }
    }
}
