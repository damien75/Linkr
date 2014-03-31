package sara.damien.app.connection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;


import sara.damien.app.Common;
import sara.damien.app.LinkrAPI;
import sara.damien.app.Profile;
import sara.damien.app.R;
import sara.damien.app.WelcomeActivity;

class LinkedInAuth { //FIXME: All these local variables should be persisted in settings to allow proper resuming
    OAuthService service;
    Token requestToken;
    String authUrl;
    String authCode;
    Token accessToken;
    String token;
    String secret;

    final static String LINKEDIN_REQUEST_URL = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,headline,picture-url,location,industry," +
            "member-url-resources,picture-urls::(original),current-status-timestamp,num-recommenders,num-connections," +
            "positions:(id,title,start-date,end-date,is-current,company:(id,name,type,size,industry,ticker))," +
            "educations:(id,school-name,field-of-study,start-date,end-date,degree))?format=json";

    public LinkedInAuth (){ //TODO: Obfuscate
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
        Common.getPrefs().setLinkedInAuthTokens(token, secret);

        authUrl = service.getAuthorizationUrl(requestToken);
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public void setAuthCode (String authCode){
        this.authCode = authCode;
    }

    public void getAccessToken() {
        accessToken = service.getAccessToken(requestToken, new Verifier(authCode));
    }

    public String getJSONProfileInfo(){
        OAuthRequest request = new OAuthRequest(Verb.GET, LINKEDIN_REQUEST_URL);
        service.signRequest(accessToken, request);
        Response response = request.send();
        return response.getBody();
    }
}


public class LinkedInConnectionActivity extends Activity {
    private class WebClientOverride extends WebViewClient {
        LinkedInAuth lna;

        public WebClientOverride (LinkedInAuth lna){
            this.lna = lna;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("linkr://")) {
                Uri uri = Uri.parse(url);
                String auth_code = uri.getQueryParameter("oauth_verifier");

                if (auth_code != null) {
                    lna.setAuthCode(auth_code);
                    new LinkedInSecondStep(lna).execute();
                } else {
                    //TODO: Something went wrong
                }

                return true;
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    private class LinkedInFirstStep extends AsyncTask<Void, Void, Void> {
        private LinkedInAuth lna;
        public LinkedInFirstStep (LinkedInAuth lna){
            this.lna = lna;
        }

        @Override
        protected Void doInBackground(Void... params) {
            lna.getRequestToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((WebView) findViewById(R.id.webv)).loadUrl(lna.getAuthUrl());
        }
    }

    public class LinkedInSecondStep extends AsyncTask <Void,Void,Void>{
        private LinkedInAuth lna;
        Profile profile;

        public LinkedInSecondStep (LinkedInAuth lna){
            this.lna = lna;
        }

        @Override
        protected Void doInBackground(Void... params) {
            lna.getAccessToken();
            String JSONProfileInfo = lna.getJSONProfileInfo();
            profile = Profile.readFromLinkedInJSON(JSONProfileInfo);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new insertProfilePicture(profile).execute();
            new searchID(profile).execute();
        }
    }

    public class searchID extends AsyncTask<Void,Void,Void> {
        String ID;
        Profile profile;
        ProgressDialog pDialog;

        public searchID(Profile profile) {
            this.profile = profile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(LinkedInConnectionActivity.this);
            pDialog.setMessage("We are checking if your profile already exists..."); //TODO: Check this message
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... args) {
            ID = LinkrAPI.matchToExistingID(profile.getLinkedInID());

            if (ID == null) { //TODO: This is a new user, show them a nice message
                pDialog.setMessage("Your profile is being created...");
                ID = LinkrAPI.createProfile(profile);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pDialog.dismiss();

            if (ID == null) {
                // TODO: Add error details
                Toast.makeText(LinkedInConnectionActivity.this, "Communication with the server failed", Toast.LENGTH_LONG).show();
            } else {
                profile.setID(ID);
                Common.getPrefs().setConnected(true);
                Common.getPrefs().setID(profile.getID()); //LATER: Store whole profile locally?
                Toast.makeText(LinkedInConnectionActivity.this, "Welcome to Linkr!", Toast.LENGTH_LONG).show();

                startActivity(new Intent(LinkedInConnectionActivity.this, WelcomeActivity.class));
                finish();
            }
        }
    }

    public class insertProfilePicture extends AsyncTask<Void,Void,Void>{
        Profile profile;

        public insertProfilePicture(Profile profile) {
            this.profile = profile;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            LinkrAPI.registerProfilePicture(profile);
            return null;
        }
    }

    // FIXME: @SuppressLint("SetJavaScriptEnabled")
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
        getMenuInflater().inflate(R.menu.linkedin_connection, menu);
        return true;
    }
}