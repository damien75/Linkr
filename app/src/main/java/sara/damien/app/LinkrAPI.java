package sara.damien.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sara.damien.app.chat.Message;
import sara.damien.app.utils.JSONParser;
import sara.damien.app.utils.Utilities;

public class LinkrAPI {
    private static String API_URL = "http://www.golinkr.net"; //TODO: Check for other occurences
    private static boolean MOCK = false;

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

    private static String getTemporaryAPIUrl() {
        return API_URL + "?_=" + UUID.randomUUID().toString();
    }

    //LATER: Implement this method as an iterator
    public static List<Message> getNewMessages(String otherParty) { //LATER: Passer une date pour latestTimeStamp
        List<Message> new_messages = new ArrayList<Message>();

        JSONParser jsonParser = new JSONParser();
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("SELECT_FUNCTION", "getNewMessages"));
        params.add(new BasicNameValuePair("chateeID", otherParty));
        params.add(new BasicNameValuePair("myID", Common.getMyID()));
        params.add(new BasicNameValuePair("timeStampReceived", Common.getPrefs().getLastReceivedMessageTimeStamp(otherParty)));
        params.add(new BasicNameValuePair("timeStampSent", Common.getPrefs().getLastSentMessageTimeStamp(otherParty)));

        JSONObject json = jsonParser.makeHttpRequest(getTemporaryAPIUrl(), "POST", params);

        try { //FIXME: Check the whole timeStamps thing
            Common.getPrefs().setLastReceivedMessageTimeStamp(json.getString("timeStampReceived"), otherParty);
            Common.getPrefs().setLastSentMessageTimeStamp(json.getString("timeStampSent"), otherParty);
            JSONArray newMessages = json.getJSONArray("msg");

            for (int msg_id = 0; msg_id < newMessages.length(); msg_id++) {
                Message message = Message.deserializeFromLinkr(newMessages.getJSONObject(msg_id));
                new_messages.add(message);
            }
        } catch (JSONException e){
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
                String timeStamp = json.getString("timeStampSent");
                message.setTime(timeStamp); //WARNING: Here we update the timestamp that allows us to show messages in the order that the server received them
                Common.getDB().insertMessage(message);
                Common.getPrefs().setLastSentMessageTimeStamp(timeStamp, message.getRecipient());
            } else {
                //Est-ce vraiment utile...?
            }
        } catch (JSONException e) {
            /*message.setID(String.valueOf(-System.currentTimeMillis()));
            message.setSent(false);
            Common.getDB().insertMessage(message);*/
            //e.printStackTrace(); //TODO: Gérer les exceptions
        }
    }

    public static boolean sendMeetingDateProposition (Meeting meeting){
        List<NameValuePair> params = meeting.serializeForLinkr();
        params.add(new BasicNameValuePair("SELECT_FUNCTION", "createMeetingDateProposition"));
        JSONObject json = new JSONParser().makeHttpRequest(API_URL,"POST",params);

        try {
            if (json.getString("success").equals("true")){
                return true;
            }
            else {
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONObject fetchDateMeetingUpdate (Meeting meeting){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("meetingID", meeting.getMeetingID()));
        params.add(new BasicNameValuePair("SELECT_FUNCTION","getStateAndDateMeeting"));
        return new JSONParser().makeHttpRequest(API_URL, "POST", params);
    }

    public static boolean updateCalendarEventID (Meeting meeting){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION","updateCalendarEventID"));
        params.add(new BasicNameValuePair("meetingID", meeting.getMeetingID()));
        params.add(new BasicNameValuePair("calendarEventID", String.valueOf(meeting.getCalendarEventID())));
        try {
            return new JSONParser().makeHttpRequest(API_URL, "POST", params).getBoolean("success");
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean acceptDateMeeting (Meeting meeting){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION","acceptDateMeeting"));
        params.add(new BasicNameValuePair("meetingID", meeting.getMeetingID()));
        try {
            return new JSONParser().makeHttpRequest(API_URL, "POST", params).getBoolean("success");
        } catch (JSONException e) {
            return false;
        }
    }

    public static boolean refuseMeetingDate (Meeting meeting){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION","refuseDateMeeting"));
        params.add(new BasicNameValuePair("meetingID", meeting.getMeetingID()));
        try {
            return new JSONParser().makeHttpRequest(API_URL, "POST", params).getBoolean("success");
        } catch (JSONException e) {
            return false;
        }
    }

    public static ArrayList<Profile> findNeighbours() {
        ArrayList<Profile> neighbours = new ArrayList<Profile>();

        if (MOCK) {
            for (int profile_id = 0; profile_id < 250; profile_id++) {
                neighbours.add(Profile.createMockProfile());
            }
            return neighbours;
        }

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfilesID2")); //TODO: Ce serait chouette de clarifier les noms des paramêtres de nos APIs.
        params.add(new BasicNameValuePair("myID", Common.getMyID()));
        params.add(new BasicNameValuePair("XU", String.valueOf(0))); //TODO: Utiliser de vraies positions (si ce sont bien des positions)
        params.add(new BasicNameValuePair("YU", String.valueOf(0)));
        params.add(new BasicNameValuePair("E", String.valueOf(1000))); //TODO: Je pense que le rayon de recherche devrait être laissé à l'appréciation du serveur.
        params.add(new BasicNameValuePair("blockedIDs", new JSONArray().toString())); //TODO: Je pense que c'est vachement risqué de faire le blocage en local.

        String json = new JSONParser().plainHttpRequest(getTemporaryAPIUrl(), "POST", params);


        try {
            JSONArray neighbourIDs = new JSONArray(json);
            for (int neighbour_id = 0; neighbour_id < neighbourIDs.length(); neighbour_id++) {
                neighbours.add(new Profile(neighbourIDs.getString(neighbour_id)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return neighbours;
    }

    // FIXME: Check whether AsyncTasks semantics allow for concurrent execution of multiple
    // asynctasks.
    public synchronized static void fillInProfiles(Iterable<Profile> toDownload) {
        if (MOCK) {
            return;
        }

        ArrayList<String> IDs = new ArrayList<String>();

        for (Profile prof : toDownload) {
            if (!prof.isDownloaded())
                IDs.add(prof.getID());
        }

        Log.i("LinkrAPI", "Filling in profiles " + Utilities.join(IDs, ", "));

        if (IDs.size() == 0)
            return;

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfilesInRange"));
        params.add(new BasicNameValuePair("IDs", new JSONArray(IDs).toString()));
        JSONObject json = new JSONParser().makeHttpRequest(getTemporaryAPIUrl(), "POST", params);

        for (Profile prof : toDownload) {
            try {
                JSONObject profile_data = json.getJSONObject(prof.getID());
                prof.setFromLinkrJSON(profile_data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap downloadProfilePicture(String ID) {
        Bitmap image = null;

        if (MOCK) { //FIXME
            image = Bitmap.createBitmap(50, 50, Bitmap.Config.RGB_565);
            Canvas p = new Canvas();
            p.setBitmap(image);
            p.drawARGB(255, 255, 255, 0);
            return image;
        }

        String url = "http://golinkr.net/get_picture.php?ID=" + ID;
        url += "&_=" + UUID.randomUUID().toString(); //FIXME: Set content expiry headers.

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            InputStream is = httpEntity.getContent();
            image = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (image == null)
            Log.e("LinkrAPI", "Website returned invalid image");

        return image;
    }
}
