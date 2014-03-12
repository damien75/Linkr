package sara.damien.app;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.chat.Message;
import sara.damien.app.utils.JSONParser;

public class LinkrAPI {
    private static String API_URL = "http://www.golinkr.net"; //TODO: Check for other occurences

    //DataBase Profile
    public static final String TAG_ID = "ID";
    public static final String TAG_LAST_NAME = "Last_Name";
    public static final String TAG_FIRST_NAME = "First_Name";

    //DataBase Meeting
    public static final String TAG_MEETING_ID = "IDm";
    public static final String TAG_ID1 = "ID1";
    public static final String TAG_ID2 = "ID2";
    public static final String TAG_SUBJECT = "Subject";
    public static final String TAG_DATE_ACCEPT = "Date_Accept";
    public static final String TAG_DATE_REQUEST = "Date_Request";
    public static final String TAG_DATE_MEETING = "Date_Meeting";
    public static final String TAG_STATE = "State";

    //DataBase Chat
    public static final String TAG_MSG_ID = "IDmsg";
    public static final String TAG_FROM_ID = "ID1";
    public static final String TAG_TO_ID = "ID2";

    public static final String TAG_MYSTATUS = "MyStatus";





    //LATER: Implement this method as an iterator
    public static List<Message> getNewMessages(String messageSender, String latestTimeStamp) { //LATER: Passer une date pour latestTimeStamp
        List<Message> new_messages = new ArrayList<Message>();

        JSONParser jsonParser = new JSONParser();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("SELECT_FUNCTION", "getLastMessage")); //TODO renommer l'api getLastMessage
        params.add(new BasicNameValuePair("ID1", messageSender));
        params.add(new BasicNameValuePair("ID2", Common.getMyID()));
        params.add(new BasicNameValuePair("Date", latestTimeStamp));

        String json = jsonParser.plainHttpRequest(API_URL, "POST", params);

        try {
            JSONArray newMessages = new JSONArray(json);

            for (int msg_id = 0; msg_id < newMessages.length(); msg_id++) {
                Message message = Message.fromJSONMessage(messageSender, Common.getMyID(), newMessages.getJSONObject(msg_id));
                new_messages.add(message);
            }
        }
        catch (JSONException e){
            e.printStackTrace();
        }

        return new_messages;
    }

    private static String IDFromResponse(JSONObject response) {
        try {
            if (response.getInt("success") == 1) {
                return response.getString("ID");
            }
        } catch (JSONException e) {}

        return null;
    }

    public static String matchToExistingID(String linkedInID) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION","existIDL"));
        params.add(new BasicNameValuePair("IDL", linkedInID));
        return IDFromResponse(new JSONParser().makeHttpRequest(API_URL, "POST", params));
    }

    public static String createProfile(Profile profile) { //TODO: Raise an excption on failure
        List<NameValuePair> params = profile.serializeForLinkr();
        params.add(new BasicNameValuePair("SELECT_FUNCTION","createProfile"));
        return IDFromResponse(new JSONParser().makeHttpRequest(API_URL, "POST", params));
    }

    public static void registerProfilePicture(Profile profile) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION","insertProfilePicture"));
        params.add(new BasicNameValuePair("Source", profile.getProfilePictureURL()));
        params.add(new BasicNameValuePair("Target", profile.getLinkedInID()));
        new JSONParser().plainHttpRequest(API_URL, "POST", params); //TODO: Handle failures
    }

    public static void SendMessage(Message message) {
        List<NameValuePair> params = message.serializeForLinkr();
        params.add(new BasicNameValuePair("SELECT_FUNCTION", "addMessage"));
        JSONObject json = new JSONParser().makeHttpRequest(API_URL,"POST",params);

        try {
            boolean isSent = json.getBoolean("success");
            if (isSent){
                message.setID(json.getString("lastID"));
                message.setSent(true);
                Common.getDB().insertMessage(message);
            } else {
                //TODO send message again
            }
        } catch (JSONException e) {
            e.printStackTrace(); //TODO: Gérer les exceptions
        }
    }
}
