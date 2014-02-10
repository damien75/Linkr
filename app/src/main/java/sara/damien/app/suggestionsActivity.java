package sara.damien.app;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class suggestionsActivity extends ListActivity {
    private static String url ="http://www.golinkr.net";
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    private static final String TAG_ID1 = "ID1";
    private static final String TAG_ID2 = "ID2";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MEETING = "meeting";
    private static final String TAG_LAST_NAME = "Last_Name";
    private static final String TAG_FIRST_NAME = "First_Name";
    private static final String TAG_STATE = "State";
    private static final String TAG_SUBJECT = "Subject";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_DATE_REQUEST = "Date_Request";
    private static final String TAG_DATE_ACCEPT = "Date_Accept";
    private static final String TAG_DATE_MEETING = "Date_Meeting";

    private static final String SELECT_FUNCTION = "getRequest";
    private static final String ID1 = "1";


    JSONArray meetings = null;
    ArrayList<HashMap<String,String>> MeetingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        MeetingList = new ArrayList<HashMap<String, String>>();
        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = ((TextView) view.findViewById(R.id.name))
                        .getText().toString();
                String cost = ((TextView) view.findViewById(R.id.state))
                        .getText().toString();
                String description = ((TextView) view.findViewById(R.id.meeting_date))
                        .getText().toString();

                // Starting single contact activity
                /*Intent in = new Intent(getApplicationContext(),
                        SingleContactActivity.class);
                in.putExtra(TAG_NAME, name);
                in.putExtra(TAG_EMAIL, cost);
                in.putExtra(TAG_PHONE_MOBILE, description);
                startActivity(in);*/
            }
        });
        // Calling async task to get json
        new GetMeetings().execute();

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }
    private class GetMeetings extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetMeetings {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(suggestionsActivity.this);
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
                            String last_name = c.getString(TAG_LAST_NAME);
                            String first_name = c.getString(TAG_FIRST_NAME);
                            String date_request = c.getString(TAG_DATE_REQUEST);
                            String date_accept = c.getString(TAG_DATE_ACCEPT);
                            String date_meeting = c.getString(TAG_DATE_MEETING);
                            String subject = c.getString(TAG_SUBJECT);
                            String state = c.getString(TAG_STATE);

                            HashMap<String,String> map = new HashMap<String, String>();
                            map.put(TAG_LAST_NAME,last_name);
                            map.put(TAG_FIRST_NAME,first_name);
                            map.put(TAG_DATE_REQUEST,date_request);
                            map.put(TAG_DATE_ACCEPT,date_accept);
                            map.put(TAG_DATE_MEETING,date_meeting);
                            map.put(TAG_SUBJECT,subject);
                            map.put(TAG_STATE, state);
                            MeetingList.add(map);
                        }
                    }
                    else{
                        Intent i = new Intent(getApplicationContext(),WelcomeActivity.class);
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
            runOnUiThread(new Runnable() {
                public void run() {
                    ListAdapter adapter = new SimpleAdapter(
                            suggestionsActivity.this,MeetingList,
                            R.layout.list_item,
                            new String[]{TAG_FIRST_NAME,TAG_LAST_NAME,TAG_DATE_REQUEST},
                            new int[]{R.id.name,R.id.state,R.id.meeting_date});
                    setListAdapter(adapter);
                }
            });
        }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.suggestions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_suggestions, container, false);
            return rootView;
        }
    }

}
