package sara.damien.app;

/**
 * Created by Damien on 26/02/2014.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEETING = "meeting";
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

    private static final String SELECT_FUNCTION = "getRequest";
    private static final String ID1="3";


    JSONArray meetings = null;
    ArrayList<HashMap<String,String>> MeetingList;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        String MeetingID = "";
        String ID1 = "3";
        String subject = "";
        Intent i = new Intent(getActivity(),SingleProfileRequestActivity.class);
        Bundle b = new Bundle();
        b.putString("ID1",ID1);
        i.putExtras(b);
        startActivity(i);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_requests_received, container, false);
        MeetingList = new ArrayList<HashMap<String, String>>();
        new GetMeetings().execute();
        return rootView;
    }
    private class GetMeetings extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetMeetings {


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
            params.add(new BasicNameValuePair("SELECT_FUNCTION",SELECT_FUNCTION));
            params.add(new BasicNameValuePair("ID1", ID1));
            JSONObject json = jsonParser.makeHttpRequest(url,"POST",params);

            // Making a request to url and getting response
            String jsonStr = json.toString();
            Log.d("Response: ", "> " + jsonStr);

            //if (jsonStr != null) {
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success==1){
                    meetings=json.getJSONArray(TAG_MEETING);
                    for (int i = 0; i<meetings.length();i++){
                        JSONObject c = meetings.getJSONObject(i);
                        String name = c.getString(TAG_FIRST_NAME)+ " " + c.getString(TAG_LAST_NAME);
                        String date_request = c.getString(TAG_DATE_REQUEST);
                        String date_accept = c.getString(TAG_DATE_ACCEPT);
                        String date_meeting = c.getString(TAG_DATE_MEETING);
                        String subject = c.getString(TAG_SUBJECT);
                        String state = c.getString(TAG_STATE);
                        String status=null;
                        String date=null;

                        if (state.contains("0")){
                            date=getString(R.string.request_sent_date)+date_request;
                            status=getString(R.string.request_status);
                        }
                        if (state.contains("1")){
                            date=getString(R.string.accepted_date)+date_accept;
                            status=getString(R.string.accepted_status);
                        }
                        if (state.contains("2")){
                            date=getString(R.string.meeting_date)+date_meeting;
                            status=getString(R.string.meeting_status);
                        }

                        HashMap<String,String> map = new HashMap<String, String>();
                        map.put(TAG_NAME,name);
                        map.put(TAG_DATE,date);
                        map.put(TAG_SUBJECT,subject);
                        map.put(TAG_STATUS, status);
                        MeetingList.add(map);
                    }
                }
                else{
                    Intent i = new Intent(getActivity(),WelcomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
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
