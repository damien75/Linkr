package sara.damien.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
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

public class DisplayProfileActivity extends FragmentActivity {
    private static String url ="http://www.golinkr.net";
    private ProgressDialog pDialog;
    static JSONParser jsonParser = new JSONParser();

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
    HashMap<String,String> MeetingList;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_demo);

        // Create an adapter that when requested, will return a fragment representing an object in
        // the collection.
        //
        // ViewPager and its adapters use support library fragments, so we must use
        // getSupportFragmentManager.
        mDemoCollectionPagerAdapter = new DemoCollectionPagerAdapter(getSupportFragmentManager());

        // Set up action bar.
        final ActionBar actionBar = getActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

        //new GetMeetings().execute();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                // Create a simple intent that starts the hierarchical parent activity and
                // use NavUtils in the Support Package to ensure proper handling of Up.
                Intent upIntent = new Intent(this, TopicActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {

        public DemoCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }



        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1); // Our object is just an integer :-P
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            // For this contrived example, we have a 100-object collection.
            return 100;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public class DemoObjectFragment extends Fragment {

        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_collection_object, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
                    Integer.toString(args.getInt(ARG_OBJECT)));
            Bundle b = getIntent().getExtras();
            Profile p = b.getParcelable("profiles");
            ((TextView) rootView.findViewById(R.id.test)).setText(p.getFirst_Name());
            return rootView;
        }


        private void startNewAsyncTask() {
            new GetMeetings(getActivity(),getArguments().getInt(ARG_OBJECT)).execute();
        }



        private class GetMeetings extends AsyncTask<Void, Void, Void>{


            JSONArray meetings = null;
            HashMap<String,String> MeetingList;
            Activity mContext;
            int arg;
            String s="";

            public GetMeetings(Activity contex, int n){
                this.mContext=contex;
                this.arg=n;
            }
            /*
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        // Showing progress dialog
                        pDialog = new ProgressDialog(CollectionDemoActivity.this);
                        pDialog.setMessage("Please wait...");
                        pDialog.setCancelable(false);
                        pDialog.show();
                    }
            */
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

                            MeetingList = new HashMap<String, String>();
                            MeetingList.put(TAG_NAME,name);
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
                super.onPostExecute(result);
                TextView txtv = (TextView) mContext.findViewById(R.id.test);
                String t1 = MeetingList.get(TAG_NAME);
                txtv.setText("wtf "+t1);
            }

        }


    }
}
