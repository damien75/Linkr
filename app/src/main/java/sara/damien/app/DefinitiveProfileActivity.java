package sara.damien.app;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.utils.JSONParser;

public class DefinitiveProfileActivity extends ActionBarActivity {
    ArrayList<Profile> profiles = new ArrayList<Profile>();
    int currentpos;
    final int currentdiff = 2;
    final int nbdownload = 4;
    final int E = 1000;
    final int XU = 0;
    final int YU = 0;
    private int currentID;
    private String currentSubject;
    private int nextFirstPos=0;
    private boolean reject=false;

    private int lastProfileShown = -1;
    private boolean pictureShown = false;

    JSONParser jsonParser = new JSONParser();
    JSONParser jsonParser2 = new JSONParser();

    private static final String url = "http://www.golinkr.net";

    JSONArray profileInfos = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definitive_profile);
        currentpos = 0;
        new ProfileIDsFinder().execute();
        ProfilesDownloader profilesDownloader = new ProfilesDownloader(nextFirstPos,nbdownload);
        profilesDownloader.execute();
        Log.d("responseeee", "executee ");
        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
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
        profiles.get(currentpos).setState(1);

        CreateMeeting CR = new CreateMeeting();
        CR.ID1 = currentID;
        //CR.ID2 = profiles.get(currentpos).getID();
        CR.subject = "Subject" + profiles.get(currentpos).getFirst_Name();
        CR.message = "";
        CR.execute();

        Button bP = (Button) findViewById(R.id.buttonProposeMeeting);
        Button bR = (Button) findViewById(R.id.buttonReject);
        TextView accept = (TextView) findViewById(R.id.textAccepted);
        bP.setVisibility(View.GONE);
        bR.setVisibility(View.GONE);
        accept.setVisibility(View.VISIBLE);
    }

    public void RejectProfile(View view) {
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
                int wrapped_pos = (preload_pos + profiles.size()) % profiles.size();
                profiles.get(wrapped_pos).downloadPicture();
            }
            //TODO: deletePicture

            //Ecrire le corps de l'affichage du fragment
            TextView name = (TextView) findViewById(R.id.profile_name);
            TextView subject = (TextView) findViewById(R.id.profile_subject);
            TextView grade = (TextView) findViewById(R.id.grade);
            TextView company = (TextView) findViewById(R.id.company);
            TextView years = (TextView) findViewById(R.id.years_experience);

            Button next = (Button) findViewById(R.id.buttonNext);

            Button bP = (Button) findViewById(R.id.buttonProposeMeeting);
            Button bR = (Button) findViewById(R.id.buttonReject);
            TextView accept = (TextView) findViewById(R.id.textAccepted);

            boolean has_profile = currentpos < profiles.size() && profiles.get(currentpos).isDownloaded();
            int visibility = has_profile ? View.VISIBLE : View.GONE;

            subject.setVisibility(visibility);
            grade.setVisibility(visibility);
            company.setVisibility(visibility);
            years.setVisibility(visibility);
            bP.setVisibility(visibility);
            bR.setVisibility(visibility);
            accept.setVisibility(View.GONE);
            next.setVisibility(visibility);


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
        int ID1;
        int ID2;
        String subject;
        String message;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                List<NameValuePair> params2 = new ArrayList<NameValuePair>();
                params2.add(new BasicNameValuePair("SELECT_FUNCTION", "createMeeting"));
                params2.add(new BasicNameValuePair("ID1", String.valueOf(ID1)));
                params2.add(new BasicNameValuePair("ID2", String.valueOf(ID2)));
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
            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfileIDs"));
            params.add(new BasicNameValuePair("XU", String.valueOf(XU)));
            params.add(new BasicNameValuePair("YU", String.valueOf(YU)));
            params.add(new BasicNameValuePair("E", String.valueOf(E)));
            String json = jsonParser.plainHttpRequest(url, "POST", params);

            // Making a request to url and getting response

            Log.d("Response: ", "> " + json);

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
                    Toast.makeText(DefinitiveProfileActivity.this, "No profile found", Toast.LENGTH_LONG).show();
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

            // Creating service handler class instance
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION", "getProfilesInRange"));
            params.add(new BasicNameValuePair("IDs", new JSONArray(IDs).toString()));
            JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);

            // Making a request to url and getting response
            String jsonStr = json.toString();
            Log.d("Response: ", "> " + jsonStr);

            //if (jsonStr != null) {

            for (int pos = firstPos; pos <= lastpos; pos++) {
                Profile prof = profiles.get(pos);

                try {
                    JSONObject data = json.getJSONObject(prof.getID());
                    prof.setProfileFromJson(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
                /*
                    Intent i = new Intent(getApplicationContext(),WelcomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                */
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
