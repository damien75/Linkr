package sara.damien.app;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Requests2Activity extends FragmentActivity implements ActionBar.TabListener {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;


    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;

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
    /*class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    update(currentpos);
                    Toast.makeText(Requests2Activity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Toast.makeText(Requests2Activity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                    update(currentpos);
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
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests2);

        // Gesture detection
        /*gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
        ViewPager r = (ViewPager)findViewById(R.id.container);
        r.setOnTouchListener(gestureListener);*/


        currentpos=0;
        GetMeetings g = new GetMeetings();
        g.execute();

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }
    public void update (int index){
        if (index==currentpos){
            //Ecrire le corps de l'affichage du fragment
            //((TextView)findViewById(android.R.id.text1)).setText("Le meeting courant est celui avec " + MeetingList.get(currentpos).get(TAG_FIRST_NAME));
            if (MeetingList.size()>currentpos){
                HashMap hashMap = MeetingList.get(currentpos);
                TextView txt1 = (TextView)findViewById(android.R.id.text1);
                txt1.setText("Your meeting is scheduled with "
                        +hashMap.get(TAG_FIRST_NAME)
                        +" "+hashMap.get(TAG_LAST_NAME));
                TextView txt2 = (TextView)findViewById(R.id.text2);
                txt2.setText("The subject of this meeting is the following: "+hashMap.get(TAG_SUBJECT));
                TextView txt3 = (TextView)findViewById(R.id.text3);
                if(hashMap.get(TAG_STATUS).equals("Processing")){
                    txt3.setText("The date requested is on "+hashMap.get(TAG_DATE));
                }
                else if(hashMap.get(TAG_STATUS).equals("Accepted")){
                    txt3.setText("The date you suggested has been accepted on "+hashMap.get(TAG_DATE));
                }
                else if(hashMap.get(TAG_STATUS).equals("Meeting scheduled")){
                    txt3.setText("The meeting is scheduled on "+hashMap.get(TAG_DATE));
                }
                else {
                    txt3.setText("There seems to be a problem with this meeting, let\'s try it all over again!");
                }
            }
            else {
                TextView txt1 = (TextView)findViewById(android.R.id.text1);
                txt1.setText("There are no more meetings to display... Plan New Meetings!!");
            }

        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        currentpos=tab.getPosition();
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DummySectionFragment();
            Bundle args = new Bundle();
            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }
    }
    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public class DummySectionFragment extends Fragment {

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_requests2, container, false);
            Bundle args = getArguments();
            int section = args.getInt(ARG_SECTION_NUMBER);
            ((TextView)rootView.findViewById(R.id.text3)).setText(((TextView)rootView.findViewById(R.id.text3)).getText()+" "+String.valueOf(section));
            /*if (!MeetingList.isEmpty()){
                if (section<4){
                    ((TextView)rootView.findViewById(android.R.id.text1)).setText("ok we are on number "
                            + args.getInt(ARG_SECTION_NUMBER)
                            + " and the corresponding meetinglist is here: "
                            + MeetingList.get(section-1).get(TAG_FIRST_NAME));
                }
                else{
                    ((TextView)rootView.findViewById(android.R.id.text1)).setText("ok we are on number "
                            + args.getInt(ARG_SECTION_NUMBER)
                            + " but the meeting list comes to an end... ");
                }

            }
            else{
                ((TextView)rootView.findViewById(android.R.id.text1)).setText("Meeting List is empty... We are on section num "+args.getInt(ARG_SECTION_NUMBER));
            }*/
            update(section);
            return rootView;
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
                    if (currentpos==0){
                        HashMap hashMap = MeetingList.get(currentpos);
                        TextView txt1 = (TextView)findViewById(android.R.id.text1);
                        txt1.setText("Your meeting is scheduled with "
                                +hashMap.get(TAG_FIRST_NAME)
                        +" "+hashMap.get(TAG_LAST_NAME));
                        TextView txt2 = (TextView)findViewById(R.id.text2);
                        txt2.setText("The subject of this meeting is the following: "+hashMap.get(TAG_SUBJECT));
                        TextView txt3 = (TextView)findViewById(R.id.text3);
                        if(hashMap.get(TAG_STATUS).equals("Processing")){
                            txt3.setText("The date requested is on "+hashMap.get(TAG_DATE));
                        }
                        else if(hashMap.get(TAG_STATUS).equals("Accepted")){
                            txt3.setText("The date you suggested has been accepted on "+hashMap.get(TAG_DATE));
                        }
                        else if(hashMap.get(TAG_STATUS).equals("Meeting scheduled")){
                            txt3.setText("The meeting is scheduled on "+hashMap.get(TAG_DATE));
                        }
                        else {
                            txt3.setText("There seems to be a problem with this meeting, let\'s try it all over again!");
                        }

                    }
                }

            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.requests2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
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

            return inflater.inflate(R.layout.fragment_requests2, container, false);
        }
    }

}