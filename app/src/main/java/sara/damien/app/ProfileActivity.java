package sara.damien.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
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

public class ProfileActivity extends ActionBarActivity {

    private static String url ="http://www.golinkr.net";
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    private static final String TAG_ID = "ID";
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PROFILE_INFO = "PROFILE_INFO";
    private static final String TAG_LAST_NAME = "Last_Name";
    private static final String TAG_FIRST_NAME = "First_Name";
    private static final String TAG_NAME = "name";
    private static final String TAG_LOC_X = "Loc_X";
    private static final String TAG_LOC_Y = "Loc_Y";
    private static final String TAG_LAST_SUBJECT = "Last_Subject";
    private static final String TAG_COMPANY = "Company";
    private static final String TAG_EXP_YEARS = "Exp_Years";
    private static final String TAG_SUM_GRADE = "Sum_Grade";
    private static final String TAG_NUMBER_GRADE = "Number_Grade";
    private static final String TAG_AVG_GRADE = "Average Grade";

    private static final String SELECT_FUNCTION = "getProfile";
    private static final String ID = "1";


    JSONArray profileInfos = null;
    HashMap<String,String> profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        profile = new HashMap<String, String>();
        /*ListView lv = getListView();
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
                Intent in = new Intent(getApplicationContext(),
                        SingleContactActivity.class);
                in.putExtra(TAG_NAME, name);
                in.putExtra(TAG_EMAIL, cost);
                in.putExtra(TAG_PHONE_MOBILE, description);
                startActivity(in);
            }
        });*/
        // Calling async task to get json
        new GetProfile().execute();

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }
    private class GetProfile extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetProfile {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION",SELECT_FUNCTION));
            params.add(new BasicNameValuePair("ID", ID));
            JSONObject json = jsonParser.makeHttpRequest(url,"POST",params);

            // Making a request to url and getting response
            String jsonStr = json.toString();
            Log.d("Response: ", "> " + jsonStr);

            //if (jsonStr != null) {
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success==1){
                    profileInfos=json.getJSONArray(TAG_PROFILE_INFO);
                    for (int i = 0; i<profileInfos.length();i++){
                        JSONObject c = profileInfos.getJSONObject(i);
                        String name = c.getString(TAG_FIRST_NAME)+ " " + c.getString(TAG_LAST_NAME);
                        String loc_x = c.getString(TAG_LOC_X);
                        String loc_y = c.getString(TAG_LOC_Y);
                        String company = c.getString(TAG_COMPANY);
                        String subject = c.getString(TAG_LAST_SUBJECT);
                        String experience = c.getString(TAG_EXP_YEARS);
                        int sum_grade = c.getInt(TAG_SUM_GRADE);
                        int number_grade = c.getInt(TAG_NUMBER_GRADE);
                        double average_grade=(double)sum_grade/((double)number_grade);
                        String avg_grade=String.valueOf(average_grade);

                        profile.put(TAG_NAME,name);
                        profile.put(TAG_LOC_X,loc_x);
                        profile.put(TAG_LOC_Y,loc_y);
                        profile.put(TAG_COMPANY,company);
                        profile.put(TAG_LAST_SUBJECT,subject);
                        profile.put(TAG_EXP_YEARS, experience);
                        profile.put(TAG_AVG_GRADE,avg_grade);
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
                    Adapter adapter = new SimpleAdapter(
                            ProfileActivity.this,profile,
                            R.layout.activity_profile,
                            new String[]{TAG_NAME,TAG_LAST_NAME,TAG_DATE_REQUEST},
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
            View rootView = inflater.inflate(R.layout.fragment_requests, container, false);
            return rootView;
        }
    }

}
