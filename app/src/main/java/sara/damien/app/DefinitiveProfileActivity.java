package sara.damien.app;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import sara.damien.app.utils.JSONParser;

public class DefinitiveProfileActivity extends ActionBarActivity {
    SharedPreferences prefs;
    ArrayList<Profile> profiles = new ArrayList<Profile>();
    int currentpos;
    final int currentdiff = 2;
    final int nbdownload = 4;
    final int E = 1000;
    final int XU = 0;
    final int YU = 0;
    private String currentID;
    private boolean lastIDDownloaded = false;
    private int nextFirstPos=0;
    private boolean reject=false;

    private int lastProfileShown = -1;
    private boolean pictureShown = false;

    JSONParser jsonParser = new JSONParser();
    JSONParser jsonParser2 = new JSONParser();

    private static final String url = "http://www.golinkr.net";

    JSONArray profileInfos = null;

    FeedProfileDbHelper mDbHelper;
    private int dbSize=0;

    ConnectionDetector cd;
    Boolean isInternetPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definitive_profile);
        currentpos = 0;

        mDbHelper = new FeedProfileDbHelper(getApplicationContext());

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        currentID = prefs.getString("ID","1");

        cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        if(isInternetPresent){
            new ProfileIDsFinder().execute();
            ProfilesDownloader profilesDownloader = new ProfilesDownloader(nextFirstPos,nbdownload);
            profilesDownloader.execute();
        }
        else {
            Log.e("local profiles downloader ","called");
            new ProfilesLocalDownLoader(nbdownload).execute();
        }
    }

    public void nextProfile(View view) {
        if (isInternetPresent){
            if (profiles.size()!=0){
            currentpos = (currentpos + 1) % profiles.size();
            Log.d("Current pos : ", "" + currentpos);
            update(currentpos);}
        }
        else {
            currentpos = (currentpos + 1) % dbSize;
            updateOffLine();
        }
    }

    public void previousProfile(View view) {
        if (isInternetPresent){
            currentpos = (currentpos - 1 + profiles.size()) % profiles.size();
            Log.d("Current pos : ", "" + currentpos);
            update(currentpos);
        }
        else {
            currentpos = (currentpos - 1 + profiles.size()) % dbSize;
            updateOffLine();
        }
    }

    public void proposeMeeting(View view) {
        if (isInternetPresent){
            profiles.get(currentpos).setState(1);

            CreateMeeting CR = new CreateMeeting();
            CR.ID1 = currentID;
            CR.ID2 = profiles.get(currentpos).getID();
            CR.subject = "Subject" + profiles.get(currentpos).getFirst_Name();
            CR.message = "";
            CR.execute();

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
            Toast.makeText(this, "You need to be connected to send invitations",Toast.LENGTH_SHORT).show();
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

    public void updateOffLine(){
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

        Log.e("has profile ",String.valueOf(currentpos)+" "+String.valueOf(dbSize)+" "+profiles.toString());
        boolean has_profile = currentpos < dbSize && profiles.get(currentpos).isDownloaded();
        int visibility = has_profile ? View.VISIBLE : View.GONE;

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
                else{
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

    private class CreateMeeting extends AsyncTask<Void, Void, Void>  {
        String ID1;
        String ID2;
        String subject;
        String message;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                List<NameValuePair> params2 = new ArrayList<NameValuePair>();
                params2.add(new BasicNameValuePair("SELECT_FUNCTION", "createMeeting"));
                params2.add(new BasicNameValuePair("ID1", ID1));
                params2.add(new BasicNameValuePair("ID2", ID2));
                params2.add(new BasicNameValuePair("Subject", subject));
                params2.add(new BasicNameValuePair("Message", message));
                JSONObject json2 = jsonParser2.makeHttpRequest(url, "POST", params2);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class ProfileIDsFinder extends AsyncTask<Void, Void, Void>  {
        @Override
        protected Void doInBackground(Void... args) {
            Set<String> blockedIDs = prefs.getStringSet("blockedIDs", new HashSet<String>());
            // Creating service handler class instance
            Log.e("ProfilesIDs","called "+new JSONArray(blockedIDs));
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfileIDs"));
            params.add(new BasicNameValuePair("myID", currentID));
            params.add(new BasicNameValuePair("XU", String.valueOf(XU)));
            params.add(new BasicNameValuePair("YU", String.valueOf(YU)));
            params.add(new BasicNameValuePair("E", String.valueOf(E)));
            params.add(new BasicNameValuePair("blockedIDs", new JSONArray(blockedIDs).toString()));
            String json = jsonParser.plainHttpRequest(url, "POST", params);

            // Making a request to url and getting response

            Log.d("Response IDs Found: ", "> " + json);

            //if (jsonStr != null) {
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

    private class ProfilesLocalDownLoader extends AsyncTask<Void,Void,Void>{

        public ProfilesLocalDownLoader(int count){
            nextFirstPos+=count;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SQLiteDatabase db = mDbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
            String[] projection = {
                    FeedProfile.FeedEntry._ID,
                    FeedProfile.FeedEntry.COLUMN_NAME_FIRST_NAME,
                    FeedProfile.FeedEntry.COLUMN_NAME_LAST_NAME,
                    FeedProfile.FeedEntry.COLUMN_NAME_LAST_SUBJECT,
                    FeedProfile.FeedEntry.COLUMN_NAME_LOC_X,
                    FeedProfile.FeedEntry.COLUMN_NAME_LOC_Y,
                    FeedProfile.FeedEntry.COLUMN_NAME_COMPANY,
                    FeedProfile.FeedEntry.COLUMN_NAME_EXP_YEARS,
                    FeedProfile.FeedEntry.COLUMN_NAME_SUM_GRADE,
                    FeedProfile.FeedEntry.COLUMN_NAME_NUMBER_GRADE
            };
            Cursor c = db.query(
                    FeedProfile.FeedEntry.TABLE_NAME,  // The table to query
                    projection,                               // The columns to return
                    "",                                // The columns for the WHERE clause
                    null,                              // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    null                                 // The sort order
            );
            dbSize += c.getCount();
            c.moveToFirst();
            Log.d("countcursor", String.valueOf(c.getColumnCount()));
            while (!c.isAfterLast()){
                Log.d("rowread", String.valueOf(c.getString(0)));
                Profile p = new Profile(true,
                        c.getString(2),
                        c.getString(1),
                        c.getString(3),
                        Integer.parseInt(c.getString(7)),
                        Double.valueOf(c.getString(4)),
                        Double.valueOf(c.getString(5)),
                        c.getString(6),
                        c.getString(0),
                        Integer.parseInt(c.getString(8)),
                        Integer.parseInt(c.getString(9)),
                        0);
                profiles.add(p);
                c.moveToNext();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            updateOffLine();
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
            Log.e("ProfilesDownloader",new JSONArray(IDs).toString());

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
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                //Delete what was previously put in the DB
                // Issue SQL statement.
                db.delete(FeedProfile.FeedEntry.TABLE_NAME, null, null);

                for (int pos = firstPos; pos <= lastpos; pos++) {
                    Profile prof = profiles.get(pos);
                    // Create a new map of values, where column names are the keys
                    ContentValues values = new ContentValues();
                    try {
                        JSONObject data = json.getJSONObject(prof.getID());
                        prof.setProfileFromJson(data);
                        values.put(FeedProfile.FeedEntry._ID,prof.getID());
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_FIRST_NAME, data.getString("First_Name"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_LAST_NAME, data.getString("Last_Name"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_LAST_SUBJECT, data.getString("Last_Subject"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_LOC_X, data.getString("Loc_X"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_LOC_Y, data.getString("Loc_Y"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_COMPANY, data.getString("Company"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_EXP_YEARS, data.getString("Exp_Years"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_SUM_GRADE, data.getString("Sum_Grade"));
                        values.put(FeedProfile.FeedEntry.COLUMN_NAME_NUMBER_GRADE, data.getString("Number_Grade"));

// Insert the new row, returning the primary key value of the new row
                        long newRowId;
                        newRowId = db.insert(
                                FeedProfile.FeedEntry.TABLE_NAME,
                                null,
                                values);
                        Log.e("ProfilesDownloader",String.valueOf(newRowId));
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
