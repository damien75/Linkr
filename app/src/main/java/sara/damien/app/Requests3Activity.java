package sara.damien.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Requests3Activity extends ActionBarActivity {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    int currentpos;

    private static String url ="http://www.golinkr.net";
    private ProgressDialog pDialog;
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
    private static final String ID1 = "1";


    JSONArray meetings = null;
    ArrayList<HashMap<String,String>> MeetingList = new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_requests0);

        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        RelativeLayout r = (RelativeLayout)findViewById(R.id.testcontainer);
        r.setOnTouchListener(gestureListener);


        currentpos=0;
        GetMeetings g = new GetMeetings();
        g.execute();

    }

    public void update (int index,int state){
        if (index==currentpos){
            int c = currentpos%2;
            if (currentpos<MeetingList.size()){
            TextView txt1 = (TextView)findViewById(R.id.sara1);
            TextView txt2 = (TextView)findViewById(R.id.sara2);
            TextView txt3 = (TextView)findViewById(R.id.sara3);
            HashMap hashMap = MeetingList.get(currentpos);
            String contact = "Your meeting is scheduled with "
                    +hashMap.get(TAG_FIRST_NAME)
                    +" "+hashMap.get(TAG_LAST_NAME);
            String subject ="The subject of this meeting is the following: "+hashMap.get(TAG_SUBJECT);
            String date;
            if(hashMap.get(TAG_STATUS).equals("Processing")){
                date="The date requested is on "+hashMap.get(TAG_DATE);
            }
            else if(hashMap.get(TAG_STATUS).equals("Accepted")){
                date="The date you suggested has been accepted on "+hashMap.get(TAG_DATE);
            }
            else if(hashMap.get(TAG_STATUS).equals("Meeting scheduled")){
                date="The meeting is scheduled on "+hashMap.get(TAG_DATE);
            }
            else {
                date="There seems to be a problem with this meeting, let\'s try it all over again!";
            }
            if(state==1){
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.swipe_right_to_left_right,R.anim.swipe_right_to_left_left);
                    DetailsFragment newFragment = new DetailsFragment().newInstance(c,contact,subject,date);
                ft.replace(R.id.testcontainer, newFragment, "detailFragment");
                    ft.commit();


            }
            else if (state==2){
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.swipe_left_to_right_left,R.anim.swipe_left_to_right_right);
                    DetailsFragment newFragment = new DetailsFragment().newInstance(c,contact,subject,date);
                ft.replace(R.id.testcontainer, newFragment, "detailFragment");
                ft.commit();
                }
                else {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(android.R.anim.fade_in);
                DetailsFragment newFragment = new DetailsFragment().newInstance(c,contact,subject,date);
                ft.replace(R.id.container,newFragment,"detailFragment");
                ft.commit();
            }
            }
            else {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(android.R.anim.fade_in);
                DetailsFragment newFragment = new DetailsFragment().newInstance(c,"No more profiles to show","","");
                ft.replace(R.id.container, newFragment, "detailFragment");
                ft.commit();

            }
        }
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(Requests3Activity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                    update(++currentpos,1);
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(Requests3Activity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    update(--currentpos,2);
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }

    private class GetMeetings extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetMeetings {


        /*@Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Requests2Activity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }*/

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
                        String first_name = c.getString(TAG_FIRST_NAME);
                        String last_name = c.getString(TAG_LAST_NAME);
                        String date_request = c.getString(TAG_DATE_REQUEST);
                        String date_accept = c.getString(TAG_DATE_ACCEPT);
                        String date_meeting = c.getString(TAG_DATE_MEETING);
                        String subject = c.getString(TAG_SUBJECT);
                        String state = c.getString(TAG_STATE);
                        String status=null;
                        String date=null;

                        if (state.contains("0")){
                            date=date_request;
                            status=getString(R.string.request_status);
                        }
                        if (state.contains("1")){
                            date=date_accept;
                            status=getString(R.string.accepted_status);
                        }
                        if (state.contains("2")){
                            date=date_meeting;
                            status=getString(R.string.meeting_status);
                        }

                        HashMap<String,String> map = new HashMap<String, String>();
                        map.put(TAG_FIRST_NAME,first_name);
                        map.put(TAG_LAST_NAME,last_name);
                        map.put(TAG_DATE,date);
                        map.put(TAG_SUBJECT,subject);
                        map.put(TAG_STATUS, status);
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
            //pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    for (int i=0; i<MeetingList.size();i++){
                        update(i,0);
                    }
                }

            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.requests3, menu);
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

    public static class DetailsFragment extends Fragment {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        public static int i;
        public static String txt1;
        public static String txt2;
        public static String txt3;
        public static DetailsFragment newInstance(int index,String contact, String subject, String date) {
            DetailsFragment f = new DetailsFragment();
            i=index;
            txt1=contact;
            txt2=subject;
            txt3=date;
            return f;
        }

        public int getShownIndex() {
            return getArguments().getInt("index", 0);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            switch (i){
                case 0:
                    rootView = inflater.inflate(R.layout.fragment_requests0, container, false);
                    break;
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_requests1, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_requests1, container, false);
                    break;
            }
            ((TextView)rootView.findViewById(R.id.sara1)).setText(txt1);
            ((TextView)rootView.findViewById(R.id.sara2)).setText(txt2);
            ((TextView)rootView.findViewById(R.id.sara3)).setText(txt3);
            return rootView;
        }
    }
}
