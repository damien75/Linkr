package sara.damien.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

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
import java.util.List;

public class TestActivity extends Activity {
    private class LinkedInAuth{
        OAuthService service;
        Token requestToken;
        String authUrl;
        String authCode;
        Token accessToken;

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
            String url2 = "https://api.linkedin.com/v1/people/~?format=json";
            OAuthRequest request = new OAuthRequest(Verb.GET, url2);
            service.signRequest(accessToken, request); // the access token from step 4
            Response response = request.send();

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
        String url;
        String id;
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
                JSONObject standardProfileRequest = jsonObject.getJSONObject("siteStandardProfileRequest");
                url=standardProfileRequest.getString("url");
                int begin = url.indexOf("=");
                int end =url.indexOf("&");
                id=url.substring(begin+1,end);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TextView txt = (TextView) findViewById(R.id.toutoutou);
            String[] s = new String[3];
            s[0]=id;
            s[1]=lastName;
            s[2]=firstName;
            new createProfile().execute(s);
            txt.setText("Hello " + firstName + " " + lastName + ". Your headline is the following: " + headline + " and your ID is: " + id);
        }
    }

    public class createProfile extends AsyncTask<String,Void,Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((TextView)findViewById(R.id.success)).setText("Your profile is being created");
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                JSONParser jsonParser = new JSONParser();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("SELECT_FUNCTION","createProfile"));
                params.add(new BasicNameValuePair("ID", strings[0]));
                params.add(new BasicNameValuePair("Last_Name",strings[1]));
                params.add(new BasicNameValuePair("First_Name",strings[2]));
                params.add(new BasicNameValuePair("Company","pipo"));
                params.add(new BasicNameValuePair("Exp_Years","1000"));
                JSONObject json = jsonParser.makeHttpRequest("http://www.golinkr.net","POST",params);
            }
            catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            //pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    TextView txt = (TextView)findViewById(R.id.success);
                    txt.setText("Your profile was successfully created!");
                }

            });
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        LinkedInAuth lna = new LinkedInAuth();

        final WebView webv = (WebView) findViewById(R.id.webv);
        webv.getSettings().setJavaScriptEnabled(true);
        webv.setWebViewClient(new WebClientOverride(lna));

        (new LinkedInFirstStep(lna)).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test, menu);
        return true;
    }
}