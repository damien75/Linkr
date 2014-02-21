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
        g.execute();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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

    public void update (int index){
        if (index==currentpos){
            if(profiles.size()- currentpos == currentdiff){
                Log.d("Response: ", "> " + currentpos);
                GetProfile g = new GetProfile();
                g.IDmin= profiles.get(profiles.size()-1).getID();
                g.execute();
            }
            //Ecrire le corps de l'affichage du fragment
            ((TextView)findViewById(R.id.txt1)).setText("Le profil courant est celui de "+profiles.get(currentpos).getFirst_Name());

        }
    }

    private class GetProfile extends AsyncTask<Void, Void, Void> implements sara.damien.app.GetProfile {
        int IDmin;

        @Override
        protected Void doInBackground(Void... args) {
            // Creating service handler class instance
            Log.d("responseeee","asynctask launched");
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","getProfileSupID"));
            params.add(new BasicNameValuePair("IDMIN",String.valueOf(this.IDmin)));
            Log.e("idmin",String.valueOf(IDmin));
            params.add(new BasicNameValuePair("XU",String.valueOf(XU)));
            params.add(new BasicNameValuePair("YU",String.valueOf(YU)));
            params.add(new BasicNameValuePair("E",String.valueOf(E)));
            params.add(new BasicNameValuePair("NBDOWN",String.valueOf(nbdownload)));
            Log.d("responseeee", "before http values"+String.valueOf(XU)+" "+String.valueOf(YU)+" "+String.valueOf(E)+" "+String.valueOf(nbdownload));
            JSONObject json = jsonParser2.makeHttpRequest(url,"POST",params);
            Log.d("responseee taille de json",String.valueOf(json.length()));

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
                        String last_name = c.getString(TAG_FIRST_NAME);
                        String first_name = c.getString(TAG_LAST_NAME);
                        Double loc_x = c.getDouble(TAG_LOC_X);
                        Double loc_y = c.getDouble(TAG_LOC_Y);
                        String company = c.getString(TAG_COMPANY);
                        String subject = c.getString(TAG_LAST_SUBJECT);
                        int experience = c.getInt(TAG_EXP_YEARS);
                        int ID = c.getInt(TAG_ID);
                        int sum_grade = c.getInt(TAG_SUM_GRADE);
                        int number_grade = c.getInt(TAG_NUMBER_GRADE);

                        Profile p = new Profile(true,last_name,first_name,subject,experience,loc_x,loc_y,company,ID,sum_grade,number_grade);
                        profiles.add(p);
                        //update(profiles.size()-1);
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
                    if(currentpos==0){
                        ((TextView)findViewById(R.id.txt1)).setText("Le premier profil est celui de " + profiles.get(0).getFirst_Name());
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
