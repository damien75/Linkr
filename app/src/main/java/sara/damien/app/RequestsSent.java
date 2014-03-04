package sara.damien.app;

/**
 * Created by Sara-Fleur on 3/3/14.
 */
public class RequestsSent {
    String Date_Request;
    String IDm;
    String ID2;
    String Subject;
    String Message;
    String First_Name;
    String Last_Name;

    public RequestsSent (String Date_Request,String IDm, String ID2, String Subject, String Message, String First_Name, String Last_Name ){
        this.Date_Request = Date_Request;
        this.IDm = IDm;
        this.ID2 = ID2;
        this.Subject = Subject;
        this.Message = Message;
        this.First_Name = First_Name;
        this.Last_Name = Last_Name;
    }

    public String getDate_Request (){return this.Date_Request;}
    public String getIDm (){return  this.IDm;}
    public String getID2 (){return  this.ID2;}
    public String getSubject (){return this.Subject;}
    public String getMessage (){return  this.Message;}
    public String getFirst_Name (){return  this.First_Name;}
    public String getLast_Name (){return this.Last_Name;}
}
