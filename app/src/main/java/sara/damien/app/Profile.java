package sara.damien.app;

import android.os.Parcel;
import android.os.Parcelable;

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
    private int ID;
    private int State;

    public Profile (boolean downloaded,String last_Name,String first_name,String last_subject, int exp_Years, double loc_X, double loc_Y, String company, int ID, int sum_Grade, int number_Grade,int State){
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
    public int getID (){return this.ID;}
    public String getLast_Subject (){return this.Last_Subject;}
    public int getState (){return this.State;}

    public void setState (int i){this.State=i;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(ID);
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
        this.ID = in.readInt();
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
}
