package sara.damien.app.connection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sara.damien.app.R;
import sara.damien.app.WelcomeActivity;
import sara.damien.app.utils.JSONParser;

public class LinkedInConnectionActivity extends Activity {
    private ProgressDialog pDialog;

    private class LinkedInAuth{
        OAuthService service;
        Token requestToken;
        String authUrl;
        String authCode;
        Token accessToken;
        String token;
        String secret;


        public LinkedInAuth (){
            service = new ServiceBuilder()
                    .provider(LinkedInApi.class)
                    .apiKey("77084e9x3e8113")
                    .apiSecret("Zor3lVQksaigLMWM")
                    .callback("linkr://golinkr.net")
                    .build();
        }

        public void getRequestToken (){
            requestToken = service.getRequestToken();
            token = requestToken.getToken();
            secret = requestToken.getSecret();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("token",token);
            editor.putString("secret",secret);
            editor.commit();
            authUrl = service.getAuthorizationUrl(requestToken);
        }

        public String getAuthUrl() {
            return authUrl;
        }

        public void setAuthCode (String s){
            this.authCode=s;
        }

        public void getAccessToken(){
            Verifier v = new Verifier(authCode);
            accessToken = service.getAccessToken(requestToken, v);
        }

        public String getProfileInfo (){
            //String url2 = "https://api.linkedin.com/v1/people/~?format=json";
            String url = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,headline,picture-url,location,industry," +
                    "member-url-resources,picture-urls::(original),current-status-timestamp,num-recommenders,num-connections," +
                    "positions:(id,title,start-date,end-date,is-current,company:(id,name,type,size,industry,ticker))," +
                    "educations:(id,school-name,field-of-study,start-date,end-date,degree))?format=json";
            OAuthRequest request = new OAuthRequest(Verb.GET, url);
            service.signRequest(accessToken, request); // the access token from step 4
            Response response = request.send();
            Log.e("picture",response.getBody());
            return response.getBody();
        }
    }

    private class WebClientOverride extends WebViewClient {
        LinkedInAuth lna;
        public WebClientOverride (LinkedInAuth ln){
            lna=ln;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("linkr://")){
                Uri uri = Uri.parse(url);
                String authcode = uri.getQueryParameter("oauth_verifier");
                if (authcode != null) {
                    lna.setAuthCode(authcode);
                    new LinkedInSecondStep(lna).execute();
                }
                return true;
            }
            else{
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    private class LinkedInFirstStep extends AsyncTask<Void, Void, Void> {
        private LinkedInAuth lnauth;
        public LinkedInFirstStep (LinkedInAuth ln){
            lnauth=ln;
        }
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        protected Void doInBackground(Void... params) {
            lnauth.getRequestToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            final WebView webv = (WebView) findViewById(R.id.webv);
            webv.loadUrl(lnauth.getAuthUrl());
        }
    }

    public class LinkedInSecondStep extends AsyncTask <Void,Void,Void>{
        private LinkedInAuth lnauth;
        String profileJsonInfos;
        String firstName;
        String lastName;
        String headline;
        String id;
        String pictureURL;
        String countrycode;
        String origin;
        String industry;
        int experience;
        public LinkedInSecondStep (LinkedInAuth ln){
            lnauth=ln;
        }

        @Override
        protected Void doInBackground(Void... params) {
            lnauth.getAccessToken();
            profileJsonInfos = lnauth.getProfileInfo();
            try {
                JSONObject jsonObject = new JSONObject(profileJsonInfos);
                firstName=jsonObject.getString("firstName");
                lastName=jsonObject.getString("lastName");
                headline=jsonObject.getString("headline");
                pictureURL = jsonObject.getJSONObject("pictureUrls").getJSONArray("values").get(0).toString();
                JSONObject positions = jsonObject.getJSONObject("positions");
                int numberofpositions = positions.getInt("_total");
                int firstposition = Calendar.getInstance().get(Calendar.YEAR);
                for (int i = 0; i<numberofpositions; i++){
                    int position = positions.getJSONArray("values").getJSONObject(i).getJSONObject("startDate").getInt("year");
                    if(position<firstposition){
                        firstposition=position;
                    }
                }
                experience = Calendar.getInstance().get(Calendar.YEAR) - firstposition;
                id=jsonObject.getString("id");
                countrycode = "";//jsonObject.getJSONObject("country").getString("code");
                origin = "";//jsonObject.getString("name");
                industry = jsonObject.getString("industry");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            insertProfilePicture insertProfilePicture = new insertProfilePicture();
            insertProfilePicture.source = pictureURL;
            insertProfilePicture.target = id;
            insertProfilePicture.execute();
            searchID searchID = new searchID();
            searchID.idl=id;
            searchID.first_name=firstName;
            searchID.last_name=lastName;
            searchID.headline=headline;
            searchID.countrycode=countrycode;
            searchID.origin=origin;
            searchID.industry=industry;
            searchID.experience=experience;
            searchID.execute();
        }
    }

    public class searchID extends AsyncTask<Void,Void,Void>{
        private String idl;
        private int userID;
        private String first_name;
        private String last_name;
        String headline;
        String countrycode;
        String origin;
        String industry;
        int experience;
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LinkedInConnectionActivity.this);
            pDialog.setMessage("We are checking if your profile already exists...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                JSONParser jsonParser = new JSONParser();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("SELECT_FUNCTION","existIDL"));
                params.add(new BasicNameValuePair("IDL", idl));
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
                public void run() { //TODO: RunOnUIThread unneeded
                    String message ="";
                    try {
                        if (json.getInt("success")==1){
                            userID = json.getInt("ID");
                            Toast.makeText(LinkedInConnectionActivity.this, "You have been connected with an already known profile with id: "+idl, Toast.LENGTH_LONG).show();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); //TODO: Use Common.getPrefs
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("Connected", true); //TODO: Add setting names into a separate enumeration/static class instead of copying strings around
                            editor.putInt("ID",userID);
                            editor.commit();
                            Intent i = new Intent(LinkedInConnectionActivity.this, WelcomeActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            createProfile createProfile = new createProfile();
                            createProfile.idl= idl;
                            createProfile.first_name=first_name;
                            createProfile.last_name=last_name;
                            createProfile.headline=headline;
                            createProfile.countrycode=countrycode;
                            createProfile.origin=origin;
                            createProfile.industry=industry;
                            createProfile.experience=experience;
                            createProfile.execute();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });
        }
    }

    public class createProfile extends AsyncTask<Void,Void,Void>{
        private String idl;
        private String userID;
        private String first_name;
        private String last_name;
        String headline;
        String countrycode;
        String origin;
        String industry;
        int experience;
        JSONObject json;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LinkedInConnectionActivity.this);
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
                params.add(new BasicNameValuePair("Company",industry));
                params.add(new BasicNameValuePair("Exp_Years",String.valueOf(experience)));
                params.add(new BasicNameValuePair("Picture",idl));
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
                            Toast.makeText(LinkedInConnectionActivity.this, "Your profile was successfully created! Welcome on Linkr!!!", Toast.LENGTH_LONG).show();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("Connected", true);
                            editor.putString("ID", userID);
                            editor.commit();
                            Intent i = new Intent(LinkedInConnectionActivity.this, WelcomeActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            Toast.makeText(LinkedInConnectionActivity.this,"Your profile was not successfully created in our database",Toast.LENGTH_LONG).show();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("Connected", false);
                            editor.commit();
                            Intent i = new Intent(LinkedInConnectionActivity.this, ConnectionTypeActivity.class);
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

    public class insertProfilePicture extends AsyncTask<Void,Void,Void>{
        String source;
        String target;

        @Override
        protected Void doInBackground(Void... voids) {
            JSONParser jsonParser = new JSONParser();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("SELECT_FUNCTION","insertProfilePicture"));
            params.add(new BasicNameValuePair("Source", source));
            params.add(new BasicNameValuePair("Target",target));
            Log.e("insertion ",source + " " + target);
            String json = jsonParser.plainHttpRequest("http://www.golinkr.net", "POST", params);
            return null;
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkedin_connection);
        LinkedInAuth lna = new LinkedInAuth();

        final WebView webv = (WebView) findViewById(R.id.webv);
        webv.getSettings().setJavaScriptEnabled(true);
        webv.setWebViewClient(new WebClientOverride(lna));

        (new LinkedInFirstStep(lna)).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.linkedin_connection, menu);
        return true;
    }
}