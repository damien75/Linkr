package sara.damien.app.requests;

/**
 * Created by Damien on 26/02/2014.
 */

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.R;
import sara.damien.app.RequestsSent;
import sara.damien.app.RequestsSentAdapter;
import sara.damien.app.utils.ConnectionDetector;
import sara.damien.app.utils.JSONParser;

public class RequestsSentFragment extends ListFragment {

    ArrayList<RequestsSent> requests;
    RequestsSentAdapter adapter;
    EditText text;
    String IDm;
    String Date_Accept;
    String myID;
    String First_Name;
    String Last_Name;
    String Subject;
    JSONParser jsonParser;
    private static String url ="http://www.golinkr.net";
    String latestTimeStampSentRequests;
    //FeedMeetingDbHelper mDbHelper;
    //SQLiteDatabase db1;
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

        requests = new ArrayList<RequestsSent>();
        if (isInternetPresent){
            new checkNewRequestsSent().execute();
        }
        else{
            new LocalRequestsCall().execute();
        }
        adapter = new RequestsSentAdapter(getActivity(), requests);
        setListAdapter(adapter);
        return rootView;
    }

    private class LocalRequestsCall extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... args) {
            requests.addAll(mDbHelper.getRequestSentMeeting(myID));
            latestTimeStampSentRequests = requests.get(0).getDate_Request();
            return null;
        }

        @Override
        protected void onPostExecute(Void text){
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

                        if (State.equals("1")){
                            mDbHelper.updateSentRequest(Date_Accept, IDm);
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
            new LocalRequestsCall().execute();
        }
    }
}
