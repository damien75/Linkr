package sara.damien.app.connection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.R;
import sara.damien.app.WelcomeActivity;
import sara.damien.app.utils.JSONParser;

public class RegisterManuallyActivity extends ActionBarActivity {
    EditText txtFirst_Name, txtLast_Name, txtCompany, txtExp_Years;
    // Register button
    Button btnRegister;

    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_manually);
    }

    public void registerManual (View view){
        txtFirst_Name = (EditText) findViewById(R.id.txtFirst_Name);
        txtLast_Name = (EditText) findViewById(R.id.txtLast_Name);
        txtCompany = (EditText) findViewById(R.id.txtCompany);
        txtExp_Years = (EditText) findViewById(R.id.txtExp_Years);
        createProfileManually CPM = new createProfileManually();
        CPM.idl="";
        CPM.first_name=txtFirst_Name.getText().toString();
        CPM.last_name=txtLast_Name.getText().toString();
        CPM.exp_years=txtExp_Years.getText().toString();
        CPM.company= txtCompany.getText().toString();
        CPM.execute();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
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

    public class createProfileManually extends AsyncTask<Void,Void,Void> {
        private String idl;
        private String userID;
        private String first_name;
        private String last_name;
        private String company;
        private String exp_years;
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterManuallyActivity.this);
            pDialog.setMessage("Your profile is being created...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                JSONParser jsonParser = new JSONParser();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("SELECT_FUNCTION","createProfile"));
                params.add(new BasicNameValuePair("IDL", idl));
                params.add(new BasicNameValuePair("Last_Name",last_name));
                params.add(new BasicNameValuePair("First_Name",first_name));
                params.add(new BasicNameValuePair("Company",company));
                params.add(new BasicNameValuePair("Exp_Years",exp_years));
                json = jsonParser.makeHttpRequest("http://www.golinkr.net","POST",params);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        if (json.getInt("success")==1){
                            userID=json.getString("ID");
                            Toast.makeText(RegisterManuallyActivity.this, "Your profile was successfully created! Welcome on Linkr!!!", Toast.LENGTH_LONG).show();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("Connected", true);
                            editor.putString("ID",userID);
                            editor.commit();
                            Intent i = new Intent(RegisterManuallyActivity.this, WelcomeActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            Toast.makeText(RegisterManuallyActivity.this,"Your profile was not successfully created in our database",Toast.LENGTH_LONG).show();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("Connected", false);
                            editor.commit();
                            Intent i = new Intent(RegisterManuallyActivity.this, ConnectionTypeActivity.class);
                            startActivity(i);
                            finish();
                        }
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            });
        }
    }

}
