package sara.damien.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TopicActivity extends ActionBarActivity {
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_LAST_SUBJECT = "Last_Subject";
    private static final String TAG_SUBJECT = "subject";
    private static String last_subject;
    EditText topic;
    String ID1="1";
    private static String url ="http://www.golinkr.net";

    JSONArray subjects = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        //new GetProfile().execute();
        topic = (EditText) findViewById(R.id.editTopic);
        last_subject = "test";
        //new getSubject().execute();
        topic.setHint(last_subject);
    }

    Profile p;
    HashMap<String,String> profile = new HashMap<String, String>();
    public void displayProfile (View view){
        /*Intent intent=new Intent(this,DisplayProfileActivity.class);
        Parcelable[] users = new Parcelable[profilestest.length];
        System.arraycopy(profilestest,0,users,0,profilestest.length);
        Bundle b = new Bundle();
        b.putParcelableArray("profiles", users);
        intent.putExtras(b);
        startActivity(intent);
        new ChooseSubject().execute();*/

        Intent i = new Intent(this,DefinitiveProfileActivity.class);
        startActivity(i);
    }

    class getSubject extends  AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... strings) {
            List<NameValuePair> params2 = new ArrayList<NameValuePair>();
            params2.add(new BasicNameValuePair("SELECT_FUNCTION","getLastSubject"));
            params2.add(new BasicNameValuePair("ID",ID1));
            JSONObject json2 = jsonParser.makeHttpRequest(url,"POST",params2);
            Log.e("getSubject","success");

            try {
                subjects=json2.getJSONArray(TAG_SUBJECT);
                for (int i = 0; i<subjects.length();i++){
                    JSONObject c = subjects.getJSONObject(i);
                    last_subject = c.getString(TAG_LAST_SUBJECT);
                    Log.e("lasst",last_subject);

                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    class ChooseSubject extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TopicActivity.this);
            pDialog.setMessage("Creating Product..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                if (topic.getText().length()>0){
                    String subject = topic.getText().toString();
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("subject", subject));
                    params.add(new BasicNameValuePair("SELECT_FUNCTION", "submitSubject"));
                    params.add(new BasicNameValuePair("ID",ID1));
                    jsonParser.makeHttpRequest(url,
                            "POST", params);
                }
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
    }

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
    private static final String ID = "3";
    private Profile[] profilestest = new Profile[10];


    JSONArray profileInfos = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.topic, menu);
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
            return inflater.inflate(R.layout.fragment_topic, container, false);
        }
    }

}
