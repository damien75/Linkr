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
import java.util.Map;

public class Requests2Activity extends FragmentActivity implements ActionBar.TabListener{
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests2);
        currentpos=0;
        GetMeetings g = new GetMeetings();
        g.execute();
        Log.d("responseee","execute ok");

        new GetMeetings().execute();

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
                currentpos=position;
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

        //ListView lv = getListView();
        //lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        /*    @Override
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
                startActivity(in);
            }
        });*/
        // Calling async task to get json

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }
    public void update (int index){
        if (index==currentpos){
            //Ecrire le corps de l'affichage du fragment
            ((TextView)findViewById(android.R.id.text1)).setText("Le meeting courant est celui avec " + MeetingList.get(currentpos).get(TAG_FIRST_NAME));

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
                        ((TextView)findViewById(android.R.id.text1)).setText("Le meeting courant est celui avec "+MeetingList.get(currentpos).get(TAG_FIRST_NAME));

                    }

                    for (Map.Entry<String, String> mapEntry : MeetingList.get(1).entrySet()) {
                        String key = mapEntry.getKey();
                        String value = mapEntry.getValue();
                        Log.e("meetinglist", key + " " + value);
                    }
                    Log.e("meetinglist", MeetingList.get(0).get(TAG_FIRST_NAME));
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