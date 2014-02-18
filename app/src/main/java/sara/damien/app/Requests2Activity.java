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
import android.widget.ListAdapter;
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

public class Requests2Activity extends FragmentActivity implements ActionBar.TabListener{

    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;

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
    ArrayList<HashMap<String,String>> MeetingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests2);

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


        MeetingList = new ArrayList<HashMap<String, String>>();
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
        new GetMeetings().execute();

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

    }

   /* public void setListAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;
    }*/

    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                /*case 0:
                    // The first section of the app is the most interesting -- it offers
                    // a launchpad into the other demonstrations in this example application.
                    return new LaunchpadSectionFragment();*/

                default:
                    // The other sections of the app are dummy placeholders.
                    Fragment fragment = new DummySectionFragment();
                    Bundle args = new Bundle();
                    args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
                    fragment.setArguments(args);
                    return fragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }
    }
    /* public class LaunchpadSectionFragment extends Fragment {

         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) {
             View rootView = inflater.inflate(R.layout.fragment_requests, container, false);

             new GetMeetings().execute();
             // Demonstration of a collection-browsing activity.
             rootView.findViewById(R.id.demo_collection_button)
                     .setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             Intent intent = new Intent(getActivity(), CollectionDemoActivity.class);
                             Bundle b = new Bundle();
                             b.putString("name", t1);
                             intent.putExtras(b);
                             startActivity(intent);
                         }
                     });

             // Demonstration of navigating to external activities.
             rootView.findViewById(R.id.demo_external_activity)
                     .setOnClickListener(new View.OnClickListener() {
                         @Override
                         public void onClick(View view) {
                             // Create an intent that asks the user to pick a photo, but using
                             // FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, ensures that relaunching
                             // the application from the device home screen does not return
                             // to the external activity.
                             Intent externalActivityIntent = new Intent(Intent.ACTION_PICK);
                             externalActivityIntent.setType("image/*");
                             externalActivityIntent.addFlags(
                                     Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                             startActivity(externalActivityIntent);
                         }
                     });

             return rootView;
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
            new GetMeetings().execute();
            Bundle args = getArguments();
            if (!MeetingList.isEmpty()){
                ((TextView) rootView.findViewById(android.R.id.text1)).setText(MeetingList.get(0).get(TAG_NAME));
            }
            else{
                ((TextView)rootView.findViewById(android.R.id.text1)).setText("Database call failed");
            }
            return rootView;
        }
    }

    private class GetMeetings extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetMeetings {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(Requests2Activity.this);
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
                            Requests2Activity.this, MeetingList,
                            R.layout.list_itemrequest,
                            new String[]{TAG_NAME, TAG_SUBJECT, TAG_STATE, TAG_DATE},
                            new int[]{R.id.name, R.id.subject, R.id.state, R.id.meeting_date});
                    //setListAdapter(adapter);
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