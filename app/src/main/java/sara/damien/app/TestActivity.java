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

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

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
            //String url2 = "http://api.linkedin.com/v1/people/id=175554962";
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
        String s;
        public LinkedInSecondStep (LinkedInAuth ln){
            lnauth=ln;
        }

        @Override
        protected Void doInBackground(Void... params) {
            lnauth.getAccessToken();
            s = lnauth.getProfileInfo();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            TextView txt = (TextView) findViewById(R.id.toutoutou);
            txt.setText(s);
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