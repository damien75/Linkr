package sara.damien.app;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DefinitiveProfileActivity extends ActionBarActivity {
    ArrayList<Profile> profiles = new ArrayList<Profile>();
    int currentpos;
    final int currentdiff = 2;
    final int nbdownload= 4;
    final int E=50;
    final int XU=0;
    final int YU=0;
    private int currentID;
    private String currentSubject;

    JSONParser jsonParser = new JSONParser();
    JSONParser jsonParser2 = new JSONParser();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PROFILE_INFO = "Profile_Info";
    private static final String TAG_LAST_NAME = "Last_Name";
    private static final String TAG_FIRST_NAME = "First_Name";
    private static final String TAG_LOC_X = "Loc_X";
    private static final String TAG_LOC_Y = "Loc_Y";
    private static final String TAG_COMPANY = "Company";
    private static final String TAG_EXP_YEARS = "Exp_Years";
    private static final String TAG_SUM_GRADE = "Sum_Grade";
    private static final String TAG_NUMBER_GRADE = "Number_Grade";
    private static final String TAG_LAST_SUBJECT = "Last_Subject";
    private static final String TAG_ID = "ID";
    private static final String TAG_SUBJECT = "subject";
    private static String url ="http://www.golinkr.net";
    private static final String ID = "3";

    JSONArray profileInfos = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definitive_profile);
        currentpos=0;
        GetProfile g = new GetProfile();
        g.IDmin= 0;
        this.currentID=1;
        this.currentSubject="ceci est un sujet";
        g.execute();
        Log.d("responseeee","executee ");
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
    }

    public void nextProfile (View view){
        if (currentpos<profiles.size()-1){
        currentpos++;
        Log.d("Current pos : ",""+ currentpos);
        update(currentpos);
        }
    }

    public void previousProfile (View view){
        if (currentpos>0){
            currentpos--;
            Log.d("Current pos : ",""+ currentpos);
            update(currentpos);
        }
    }

    public void CreateMeeting(View view){
        profiles.get(currentpos).setState(1);

        CreateMeeting CR = new CreateMeeting();
        CR.ID1=currentID;
        CR.ID2=profiles.get(currentpos).getID();
        CR.subject="Subject"+profiles.get(currentpos).getFirst_Name();
        CR.message="";
        CR.execute();

        Button bP = (Button) findViewById(R.id.buttonProposeMeeting);
        Button bR = (Button) findViewById(R.id.buttonReject);
        TextView accept = (TextView) findViewById(R.id.textAccepted);
        bP.setVisibility(View.GONE);
        bR.setVisibility(View.GONE);
        accept.setVisibility(View.VISIBLE);
    }

    public void RejectProfile(View view){
        profiles.remove(currentpos);
        update(currentpos);
    }

    public void update (int index){
        if (index==currentpos){
            if(profiles.size()- currentpos == currentdiff){
                Log.d("Response: ", "> " + currentpos);
                GetProfile g = new GetProfile();
                g.IDmin= profiles.get(profiles.size()-1).getID();
                g.execute();
            }
            //Ecrire le corps de l'affichage du fragment
            TextView name = (TextView)findViewById(R.id.profile_name);
            TextView subject = (TextView)findViewById(R.id.profile_subject);
            TextView grade = (TextView)findViewById(R.id.grade);
            TextView company = (TextView) findViewById(R.id.company);
            TextView years = (TextView) findViewById(R.id.years_experience);

            Button previous = (Button) findViewById(R.id.buttonPrevious);
            Button next = (Button) findViewById(R.id.buttonNext);

            Button bP = (Button) findViewById(R.id.buttonProposeMeeting);
            Button bR = (Button) findViewById(R.id.buttonReject);
            TextView accept = (TextView) findViewById(R.id.textAccepted);

            if (currentpos>=profiles.size()){
                name.setText("Plus de profiles disponibles");
                subject.setVisibility(View.GONE);
                grade.setVisibility(View.GONE);
                company.setVisibility(View.GONE);
                years.setVisibility(View.GONE);
                bP.setVisibility(View.GONE);
                bR.setVisibility(View.GONE);
                accept.setVisibility(View.GONE);
                next.setVisibility(View.GONE);
            }
            else{
            name.setText(profiles.get(currentpos).getFirst_Name()+" "+profiles.get(currentpos).getLast_Name());
            subject.setText(profiles.get(currentpos).getLast_Subject());
            grade.setText(profiles.get(currentpos).get_Avg_Grade());
            company.setText(profiles.get(currentpos).getCompany());
            years.setText(profiles.get(currentpos).getExp_Years());


            if (profiles.get(currentpos).getState()==0){
                bP.setVisibility(1);
                bR.setVisibility(1);
                accept.setVisibility(View.GONE);
            }
            else{
                bP.setVisibility(View.GONE);
                bR.setVisibility(View.GONE);
                accept.setVisibility(View.VISIBLE);
            }
            }

        }
    }

    private class CreateMeeting extends AsyncTask<Void,Void,Void> implements sara.damien.app.GetProfile{
        int ID1;
        int ID2;
        String subject;
        String message;

        @Override
        protected Void doInBackground(Void... params) {
            try{
            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            params2.add(new BasicNameValuePair("SELECT_FUNCTION","createMeeting"));
            params2.add(new BasicNameValuePair("ID1",String.valueOf(ID1)));
            params2.add(new BasicNameValuePair("ID2",String.valueOf(ID2)));
            params2.add(new BasicNameValuePair("Subject",subject));
            params2.add(new BasicNameValuePair("Message",message));
            JSONObject json2 = jsonParser2.makeHttpRequest(url,"POST",params2);}
            catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetProfile extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetProfile {
        int IDmin;

        @Override
        protected Void doInBackground(Void... args) {
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","getProfileSupID"));
            params.add(new BasicNameValuePair("IDMIN",String.valueOf(this.IDmin)));
            params.add(new BasicNameValuePair("XU",String.valueOf(XU)));
            params.add(new BasicNameValuePair("YU",String.valueOf(YU)));
            params.add(new BasicNameValuePair("E",String.valueOf(E)));
            params.add(new BasicNameValuePair("NBDOWN", String.valueOf(nbdownload)));
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
                        String first_name = c.getString(TAG_FIRST_NAME);
                        String last_name = c.getString(TAG_LAST_NAME);
                        Double loc_x = c.getDouble(TAG_LOC_X);
                        Double loc_y = c.getDouble(TAG_LOC_Y);
                        String company = c.getString(TAG_COMPANY);
                        String subject = c.getString(TAG_LAST_SUBJECT);
                        int experience = c.getInt(TAG_EXP_YEARS);
                        int ID = c.getInt(TAG_ID);
                        int sum_grade = c.getInt(TAG_SUM_GRADE);
                        int number_grade = c.getInt(TAG_NUMBER_GRADE);
                        int state=0;

                        Profile p = new Profile(true,last_name,first_name,subject,experience,loc_x,loc_y,company,ID,sum_grade,number_grade,state);
                        profiles.add(p);
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
            runOnUiThread(new Runnable() {
                public void run() {
                    for (int i=0;i<nbdownload-1;i++){
                        update(i);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.definitive_profile, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_definitive_profile, container, false);

            return rootView;
        }
    }


}
