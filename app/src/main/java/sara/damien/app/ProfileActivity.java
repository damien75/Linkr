package sara.damien.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

public class ProfileActivity extends Activity {

    private static String url ="http://www.golinkr.net";
    private ProgressDialog pDialog;
    JSONParser jsonParser = new JSONParser();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PROFILE_INFO = "Profile_Info";
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
    private static final String ID = "2";


    JSONArray profileInfos = null;
    HashMap<String,String> profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile = new HashMap<String, String>();
        // Calling async task to get json
        new GetProfile().execute();
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
                        profile.put(TAG_LOC_X, loc_x);
                        profile.put(TAG_LOC_Y, loc_y);
                        profile.put(TAG_COMPANY, company);
                        profile.put(TAG_LAST_SUBJECT, subject);
                        profile.put(TAG_EXP_YEARS, experience);
                        profile.put(TAG_AVG_GRADE, avg_grade);
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
                    TextView txt1 = (TextView) findViewById(R.id.textView);
                    TextView txt2 = (TextView) findViewById(R.id.textView2);
                    TextView txt3 = (TextView) findViewById(R.id.grade);
                    TextView txt4 = (TextView) findViewById(R.id.company);
                    TextView txt5 = (TextView) findViewById(R.id.years_experience);
                    TextView txt6 = (TextView) findViewById(R.id.textView3);
                    String t1 = profile.get(TAG_LAST_SUBJECT);
                    String t2 = profile.get(TAG_NAME);
                    String t3 = txt3.getText() + profile.get(TAG_AVG_GRADE);
                    String t4 = txt4.getText() + profile.get(TAG_COMPANY);
                    String t5 = txt5.getText() + profile.get(TAG_EXP_YEARS);
                    String t6 = "My current position is the following: X="+profile.get(TAG_LOC_X)+" and Y="+profile.get(TAG_LOC_Y);
                    txt1.setText(t1);
                    txt2.setText(t2);
                    txt3.setText(t3);
                    txt4.setText(t4);
                    txt5.setText(t5);
                    txt6.setText(t6);
                }
            });
        }

    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.requests, menu);
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
            return inflater.inflate(R.layout.fragment_requests, container, false);
        }
    }

}