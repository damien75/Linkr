package sara.damien.app;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SingleProfileRequestActivity extends ActionBarActivity {
    private String MeetingID;
    private String IDu;
    private String Subject;
    private Profile requestedProfile;
    private double latitude;
    private double longitude;
    private boolean gpsPositionKnown=false;
    JSONParser jsonParser = new JSONParser();
    String url = "http://www.golinkr.net";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_profile_request);

        (findViewById(R.id.textAccepted)).setVisibility(View.GONE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getBoolean("LocationFound",false)){
            gpsPositionKnown=true;
            latitude = (double)prefs.getFloat("Latitude",0);
            longitude = (double)prefs.getFloat("Longitude",0);
        }
        Bundle b = getIntent().getExtras();
        IDu = b.getString("IDu");
        MeetingID = b.getString("IDm");
        Subject = b.getString("Subject");
        ((TextView)findViewById(R.id.profile_subject)).setText("# "+Subject);
        new getProfileRequest().execute();
    }

    public void acceptMeeting(View view){
        new acceptMeeting().execute();
    }
    public void refuseMeeting(View view){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_profile_request, menu);
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

    public class getProfileRequest extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            requestedProfile = new Profile (IDu);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfile"));
            params.add(new BasicNameValuePair("ID", IDu));
            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);
            requestedProfile.setProfileFromJson(json);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            ((TextView) findViewById(R.id.profile_name)).setText(requestedProfile.getFirst_Name() + " " + requestedProfile.getLast_Name());
            TextView grade = (TextView) findViewById(R.id.grade);
            grade.setText(grade.getText() + " " + requestedProfile.get_Avg_Grade());
            TextView company = (TextView) findViewById(R.id.company);
            company.setText(company.getText() + " " + requestedProfile.getCompany());
            TextView years = (TextView) findViewById(R.id.years_experience);
            years.setText(years.getText() + " " + requestedProfile.getExp_Years());
            TextView distance = (TextView)findViewById(R.id.profile_position);
            double longi1 = requestedProfile.getLoc_X();
            double lat1 = requestedProfile.getLoc_Y();
            double longi2 = longitude;
            double lat2 = latitude;

            if(gpsPositionKnown){
                double d = Math.round(distance(lat1,longi1,lat2,longi2)/1000.0);
                distance.setText(distance.getText() + " " + String.valueOf(d)+" km");
            }
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public class acceptMeeting extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "acceptProposition"));
            params.add(new BasicNameValuePair("IDm", MeetingID));
            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            (findViewById(R.id.textAccepted)).setVisibility(View.VISIBLE);
        }
    }
}
