package sara.damien.app.requests;

/**
 * Created by Damien on 26/02/2014.
 */

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.Meeting;
import sara.damien.app.R;
import sara.damien.app.adapter.SentRequestsAdapter;
import sara.damien.app.utils.ConnectionDetector;
import sara.damien.app.utils.JSONParser;

public class RequestsSentFragment extends ListFragment {

    ArrayList<Meeting> requests;
    JSONArray meetings;
    SentRequestsAdapter adapter;
    String myID;
    JSONParser jsonParser;
    private static String url ="http://www.golinkr.net";
    String latestTimeStampSentRequests;
    DbHelper mDbHelper;

    ConnectionDetector cd;
    boolean isInternetPresent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_requests_sent, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        myID = prefs.getString("ID","0");
        latestTimeStampSentRequests = prefs.getString("TimeStampSentRequests", "2014-02-28 16:27:40");

        mDbHelper = new DbHelper(getActivity().getApplicationContext());
        //db1 = mDbHelper.getReadableDatabase();
        cd = new ConnectionDetector(getActivity().getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();

        requests = new ArrayList<Meeting>();
        if (isInternetPresent){
            jsonParser = new JSONParser();
            new GetRequestsSent().execute();
            new checkNewRequestsSent().execute();
        }
        else{
            new LocalRequestsCall().execute();
        }
        adapter = new SentRequestsAdapter(getActivity(), requests);
        setListAdapter(adapter);
        return rootView;
    }

    private class LocalRequestsCall extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... args) {
            requests.addAll(mDbHelper.getRequestSentMeeting(myID));
            return null;
        }

        @Override
        protected void onPostExecute(Void text){
            adapter.notifyDataSetChanged();
        }
    }

    private class GetRequestsSent extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","getSentRequests"));
            params.add(new BasicNameValuePair("ID1", myID));
            String jsonStr = jsonParser.plainHttpRequest(url,"POST",params);

            // Making a request to url and getting response
            //String jsonStr = json.toString();
            Log.d("Response: ", "> " + jsonStr);

            //if (jsonStr != null) {
            try {
                meetings = new JSONArray(jsonStr);
                if (meetings.length()>0){
                    for (int i = 0; i<meetings.length();i++){
                        JSONObject c = meetings.getJSONObject(i);
                        String first_name = c.getString("First_Name");
                        String last_name = c.getString("Last_Name");
                        String date_request = c.getString("Date_Request");
                        String subject = c.getString("Subject");
                        String idu = c.getString("ID");
                        String idm = c.getString("IDm");
                        String message = c.getString("Message");

                        Meeting request = new Meeting (date_request,idm,idu,subject,message,first_name,last_name);
                        requests.add(request);
                        Log.d("prénom requete",requests.get(i).getOtherParticipant().getFirst_Name());
                        mDbHelper.insertLocalRequestSentMeeting(idm, myID, idu, subject, "0", message);
                    }
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return null;

        }
        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
        }

    }

    private class checkNewRequestsSent extends AsyncTask<Void, String, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "checkUpdateSentRequests"));
            params.add(new BasicNameValuePair("myID", myID));
            params.add(new BasicNameValuePair("timestamp",latestTimeStampSentRequests));
            String json = jsonParser.plainHttpRequest(url,"POST",params);
            try{
                JSONArray newRequests = new JSONArray(json);
                if (newRequests.length()>0){
                    latestTimeStampSentRequests=newRequests.getJSONObject(0).getString("Date_Accept");
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("TimeStampSentRequest", latestTimeStampSentRequests);
                    editor.commit();
                    for (int i=0; i<newRequests.length(); i++){
                        JSONObject request = newRequests.getJSONObject(i);
                        String State = request.getString("State");
                        String IDm = request.getString("IDm");
                        String Date_Accept = request.getString("Date_Accept");
                        String ID2 = request.getString("ID");
                        String Subject = request.getString("Subject");
                        String Date_Request = request.getString("Date_Request");
                        String Message = request.getString("Message");

                        if (State.equals("1")){
                            mDbHelper.updateSentRequest(Date_Accept, IDm,myID,ID2,Subject,Date_Request,Message);
                        }
                        else{
                            mDbHelper.deleteSentRequest(IDm);
                        }
                    }
                }}
            catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            //new LocalRequestsCall().execute();
        }
    }
}