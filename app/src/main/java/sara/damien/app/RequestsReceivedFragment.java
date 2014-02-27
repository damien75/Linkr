package sara.damien.app;

/**
 * Created by Damien on 26/02/2014.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestsReceivedFragment extends ListFragment {
    private static String url ="http://www.golinkr.net";
    ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    private static final String TAG_LAST_NAME = "Last_Name";
    private static final String TAG_FIRST_NAME = "First_Name";
    private static final String TAG_NAME = "name";
    private static final String TAG_STATE = "State";
    private static final String TAG_SUBJECT = "Subject";
    private static final String TAG_DATE_REQUEST = "Date_Request";
    private static final String TAG_DATE_ACCEPT = "Date_Accept";
    private static final String TAG_DATE_MEETING = "Date_Meeting";
    private static final String TAG_DATE= "Date";
    private static final String TAG_STATUS= "Status";
    private static final String TAG_ID = "ID";
    private static final String TAG_IDm = "IDm";
    private static String ID2 = "2";


    JSONArray meetings = null;
    ArrayList<HashMap<String,String>> MeetingList;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        HashMap<String,String>meeting = MeetingList.get(position);
        String MeetingID = meeting.get(TAG_IDm);
        String IDu = meeting.get(TAG_ID);
        String subject = meeting.get(TAG_SUBJECT);
        Intent i = new Intent(getActivity(),SingleProfileRequestActivity.class);
        Bundle b = new Bundle();
        b.putString("IDu",IDu);
        b.putString("IDm",MeetingID);
        b.putString("Subject",subject);
        i.putExtras(b);
        startActivity(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_requests_received, container, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        ID2 = prefs.getString("ID","2");

        MeetingList = new ArrayList<HashMap<String, String>>();
        new GetMeetings().execute();
        return rootView;
    }
    private class GetMeetings extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","getReceivedRequests"));
            params.add(new BasicNameValuePair("ID2", ID2));
            String jsonStr = jsonParser.plainHttpRequest(url,"POST",params);

            // Making a request to url and getting response
            //String jsonStr = json.toString();
            Log.d("Response: ", "> " + jsonStr);

            //if (jsonStr != null) {
            try {
                meetings= new JSONArray(jsonStr);
                if (meetings.length()>0){
                    for (int i = 0; i<meetings.length();i++){
                        JSONObject c = meetings.getJSONObject(i);
                        String name = c.getString(TAG_FIRST_NAME)+ " " + c.getString(TAG_LAST_NAME);
                        String date_request = c.getString(TAG_DATE_REQUEST);
                        String subject = c.getString(TAG_SUBJECT);
                        String idu = c.getString(TAG_ID);
                        String idm = c.getString(TAG_IDm);
                        String date=getString(R.string.request_sent_date)+date_request;
                        String status=getString(R.string.request_status);

                        HashMap<String,String> map = new HashMap<String, String>();
                        map.put(TAG_NAME,name);
                        map.put(TAG_DATE,date);
                        map.put(TAG_SUBJECT,subject);
                        map.put(TAG_STATUS, status);
                        map.put(TAG_ID,idu);
                        map.put(TAG_IDm,idm);
                        MeetingList.add(map);
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
            pDialog.dismiss();
            ListAdapter adapter = new SimpleAdapter(
                    getActivity(), MeetingList,
                    R.layout.list_itemrequest,
                    new String[]{TAG_NAME, TAG_SUBJECT, TAG_STATE, TAG_DATE},
                    new int[]{R.id.name, R.id.subject, R.id.state, R.id.meeting_date});
            setListAdapter(adapter);
        }

    }
}
