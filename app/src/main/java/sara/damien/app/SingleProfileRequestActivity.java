package sara.damien.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SingleProfileRequestActivity extends ActionBarActivity {
    public String MeetingID;
    public String ID1;
    private String subject;
    private Profile requestedProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_profile_request);

        Bundle b = getIntent().getExtras();
        ID1 = b.getString("ID1");
        Toast.makeText(this,ID1,Toast.LENGTH_LONG).show();
        new getProfileRequest().execute();
    }

    public class getProfileRequest extends AsyncTask<Void,Void,Void>{
        JSONParser jsonParser = new JSONParser();
        String url = "http://www.golinkr.net";

        @Override
        protected Void doInBackground(Void... voids) {
            requestedProfile = new Profile (ID1);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfile"));
            params.add(new BasicNameValuePair("ID", ID1));
            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);
            Log.e("backg",json.toString());
            requestedProfile.setProfileFromJson(json);
            return null;
        }

        @Override
        protected void onPostExecute(Void result){
            ((TextView) findViewById(R.id.profile_name)).setText(requestedProfile.getFirst_Name() + " " + requestedProfile.getLast_Name());
            ((TextView) findViewById(R.id.profile_subject)).setText(requestedProfile.getLast_Subject());
            TextView grade = (TextView) findViewById(R.id.grade);
            grade.setText(grade.getText() + " " + requestedProfile.get_Avg_Grade());
            TextView company = (TextView) findViewById(R.id.company);
            company.setText(company.getText() + " " + requestedProfile.getCompany());
            TextView years = (TextView) findViewById(R.id.years_experience);
            years.setText(years.getText() + " " + requestedProfile.getExp_Years());
            TextView accept = (TextView) findViewById(R.id.textAccepted);
            TextView distance = (TextView)findViewById(R.id.profile_position);
            double longi = requestedProfile.getLoc_X()*Math.PI/180;
            double lat = requestedProfile.getLoc_Y()*Math.PI/180;
            /*double d = 6371*2*Math; // km
            var dLat = (lat2-lat1).toRad();
            var dLon = (lon2-lon1).toRad();
            var lat1 = lat1.toRad();
            var lat2 = lat2.toRad();

            var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
            var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            var d = R * c;

            distance.setText(distance.getText() + " ");*/
            accept.setVisibility(View.GONE);
        }
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

}
