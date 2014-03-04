package sara.damien.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.utils.ConnectionDetector;
import sara.damien.app.utils.JSONParser;

public class DefinitiveProfileActivity extends ActionBarActivity {
    String subject;
    SharedPreferences prefs;
    ArrayList<Profile> profiles = new ArrayList<Profile>();
    int currentpos;
    final int currentdiff = 2;
    final int nbdownload = 4;
    final int E = 1000;
    final int XU = 0;
    final int YU = 0;
    private String myID;
    private boolean lastIDDownloaded = false;
    private int nextFirstPos=0;
    private boolean reject=false;

    private int lastProfileShown = -1;
    private boolean pictureShown = false;

    JSONParser jsonParser = new JSONParser();
    JSONParser jsonParser2 = new JSONParser();

    private static final String url = "http://www.golinkr.net";

    JSONArray profileInfos = null;

    //FeedProfileDbHelper mDbProfileHelper;
    //FeedMeetingDbHelper mDbMeetingHelper;
    DbHelper mDbHelper;

    ConnectionDetector cd;
    boolean isInternetPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definitive_profile);
        currentpos = 0;
        subject = getIntent().getStringExtra("subject");

        //mDbProfileHelper = new FeedProfileDbHelper(getApplicationContext());
        //mDbMeetingHelper = new FeedMeetingDbHelper(getApplicationContext());
        mDbHelper = new DbHelper(getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myID = prefs.getString("ID","0");

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if(isInternetPresent){
            new ProfileIDsFinder().execute();
            ProfilesDownloader profilesDownloader = new ProfilesDownloader(nextFirstPos,nbdownload);
            profilesDownloader.execute();
        }
    }

    public void nextProfile(View view) {
            currentpos = (currentpos + 1) % profiles.size();
            Log.d("Current pos : ", "" + currentpos);
            update(currentpos);

    }

    public void previousProfile(View view) {
            currentpos = (currentpos - 1 + profiles.size()) % profiles.size();
            Log.d("Current pos : ", "" + currentpos);
            update(currentpos);
    }

    public void proposeMeeting(View view) {
        if (isInternetPresent){
            Profile profile = profiles.get(currentpos);
            profile.setState(1);

            CreateMeeting CR = new CreateMeeting();
            CR.ID1 = myID;
            CR.ID2 = profiles.get(currentpos).getID();
            CR.subject = subject;
            CR.message = "";
            CR.execute();
        }
        else {
            Toast.makeText(this, "You need to be connected to send invitations", Toast.LENGTH_SHORT).show();
        }
    }

    public void rejectProfile(View view) {
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> blockedIDs = prefs.getStringSet("blockedIDs", new HashSet<String>());
        blockedIDs.add(profiles.get(currentpos).getID());
        Log.e("BlockedIDs", blockedIDs.toString());
        editor.putStringSet("blockedIDs", blockedIDs);
        editor.commit();
        profiles.remove(currentpos);
        reject=true;
        nextFirstPos--;
        update(currentpos);
    }

    public void notNow(){
        profiles.remove(currentpos);
        reject=true;
        nextFirstPos--;
        update(currentpos);
    }


    public void update(int index) {
        if (index == currentpos) {
            if (nextFirstPos - currentpos == currentdiff) {
                Log.d("Response: ", "> " + currentpos);
                ProfilesDownloader g = new ProfilesDownloader(nextFirstPos,nbdownload);
                g.execute();
            }

            for (int preload_pos = currentpos - 2; preload_pos <= currentpos + 2; preload_pos++) {
                if (profiles.size()>0){
                    int wrapped_pos = (preload_pos + profiles.size()) % profiles.size();
                    profiles.get(wrapped_pos).downloadPicture();
                }
            }

            //Ecrire le corps de l'affichage du fragment
            TextView name = (TextView) findViewById(R.id.profile_name);
            TextView subject = (TextView) findViewById(R.id.profile_subject);
            TextView grade = (TextView) findViewById(R.id.grade);
            TextView company = (TextView) findViewById(R.id.company);
            TextView years = (TextView) findViewById(R.id.years_experience);

            Button next = (Button) findViewById(R.id.buttonNext);
            Button previous = (Button) findViewById(R.id.buttonPrevious);

            Button bP = (Button) findViewById(R.id.buttonProposeMeeting);
            Button bR = (Button) findViewById(R.id.buttonReject);
            Button bN = (Button) findViewById(R.id.neverEver);
            TextView accept = (TextView) findViewById(R.id.textAccepted);

            boolean has_profile = currentpos < profiles.size() && profiles.get(currentpos).isDownloaded();
            int visibility = has_profile ? View.VISIBLE : View.GONE;
            if (currentpos==0 && has_profile && !lastIDDownloaded){
                previous.setEnabled(false);
            }
            else {
                previous.setEnabled(true);
            }

            subject.setVisibility(visibility);
            grade.setVisibility(visibility);
            company.setVisibility(visibility);
            years.setVisibility(visibility);
            bP.setVisibility(visibility);
            bR.setVisibility(visibility);
            accept.setVisibility(View.GONE);
            next.setVisibility(visibility);
            previous.setVisibility(visibility);
            bN.setVisibility(visibility);


            if (!has_profile) {
                lastProfileShown = -1;
                name.setText("Plus de profiles disponibles");
            } else {
                Profile prof = profiles.get(currentpos);

                if (currentpos != lastProfileShown ||reject) {
                    lastProfileShown = currentpos;
                    reject=false;
                    pictureShown = false;

                    name.setText(prof.getFirst_Name() + " " + prof.getLast_Name());
                    subject.setText(prof.getLast_Subject());
                    grade.setText(prof.get_Avg_Grade());
                    company.setText(prof.getCompany());
                    years.setText(prof.getExp_Years());

                    if (prof.getState() != 0) {
                        bP.setVisibility(View.GONE);
                        bR.setVisibility(View.GONE);
                        accept.setVisibility(View.VISIBLE);
                    }

                }

                if (!pictureShown) {
                    Bitmap picture = prof.getPicture();
                    ImageView view = (ImageView) findViewById(R.id.profile_picture);

                    if (picture != null) {
                        pictureShown = true;
                        view.setImageBitmap(picture);
                    } else
                        view.setImageResource(R.drawable.lil_wayne);
                }
            }
        }
    }

    private class CreateMeeting extends AsyncTask<Void, Void, Boolean>  {
        String ID1;
        String ID2;
        String subject;
        String message;
        String IDm;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                List<NameValuePair> params2 = new ArrayList<NameValuePair>();
                params2.add(new BasicNameValuePair("SELECT_FUNCTION", "createMeeting"));
                params2.add(new BasicNameValuePair("ID1", ID1));
                params2.add(new BasicNameValuePair("ID2", ID2));
                params2.add(new BasicNameValuePair("Subject", subject));
                params2.add(new BasicNameValuePair("Message", message));
                JSONObject json2 = jsonParser2.makeHttpRequest(url, "POST", params2);
                if (json2.getString("success").equals("1")){
                    IDm = json2.getString("ID");
                    Profile profile = profiles.get(currentpos);
                    mDbHelper.insertLocalProfile(profile);
                    mDbHelper.insertLocalRequestSentMeeting(IDm,ID1,ID2,subject,"0","no message");
                    mDbHelper.getRequestSentMeeting(myID);
                    return true;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                Button bP = (Button) findViewById(R.id.buttonProposeMeeting);
                Button bR = (Button) findViewById(R.id.buttonReject);
                Button bN = (Button) findViewById(R.id.neverEver);
                TextView accept = (TextView) findViewById(R.id.textAccepted);
                bP.setVisibility(View.GONE);
                bR.setVisibility(View.GONE);
                bN.setVisibility(View.GONE);
                accept.setVisibility(View.VISIBLE);
            }
            else {
                Toast.makeText(getApplicationContext(),"Bad connection, try again later...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ProfileIDsFinder extends AsyncTask<Void, Void, Void>  {
        @Override
        protected Void doInBackground(Void... args) {
            Set<String> blockedIDs = prefs.getStringSet("blockedIDs", new HashSet<String>());
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfilesID2"));
            params.add(new BasicNameValuePair("myID", myID));
            params.add(new BasicNameValuePair("XU", String.valueOf(XU)));
            params.add(new BasicNameValuePair("YU", String.valueOf(YU)));
            params.add(new BasicNameValuePair("E", String.valueOf(E)));
            params.add(new BasicNameValuePair("blockedIDs", new JSONArray(blockedIDs).toString()));
            String json = jsonParser.plainHttpRequest(url, "POST", params);

            // Making a request to url and getting response

            Log.d("Response IDs Found: ", "> " + json);
            try {
                profileInfos = new JSONArray(json);
                if (profileInfos.length() > 0) {
                    for (int i = 0; i < profileInfos.length(); i++) {
                        String c = profileInfos.getString(i);
                        Profile p = new Profile(c, DefinitiveProfileActivity.this);
                        profiles.add(p);
                    }
                } else {
                    Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


    private class ProfilesDownloader extends AsyncTask<Void, Void, Void>  {
        int firstPos, count;

        public ProfilesDownloader(int firstPos, int count) {
            nextFirstPos += count;
            this.firstPos = firstPos;
            this.count = count;
        }

        @Override
        protected Void doInBackground(Void... args) {
            int lastpos = Math.min(firstPos + count, profiles.size()) - 1;
            ArrayList<String> IDs = new ArrayList<String>(count);
            for (int pos = firstPos; pos <= lastpos; pos++) {
                Profile prof = profiles.get(pos);
                if (!prof.isDownloaded())
                    IDs.add(prof.getID());
            }
            Log.e("ProfilesDownloader",IDs + " "+ new JSONArray(IDs).toString());

            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfilesInRange"));
            params.add(new BasicNameValuePair("IDs", new JSONArray(IDs).toString()));
            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);

            // Making a request to url and getting response
            String jsonStr = json.toString();
            Log.d("Response: ", "> " + jsonStr);

            if (json.length()>0) {
                if(lastpos==profiles.size()-1){
                    lastIDDownloaded=true;
                }

                for (int pos = firstPos; pos <= lastpos; pos++) {
                    Profile prof = profiles.get(pos);
                    try {
                        JSONObject data = json.getJSONObject(prof.getID());
                        prof.setProfileFromJson(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                Log.e("ProfilesDownloader", "No profiles corresponding to the range of IDs given fetched");
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {
            update(currentpos);
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
            return inflater.inflate(R.layout.fragment_definitive_profile, container, false);
        }
    }
}
