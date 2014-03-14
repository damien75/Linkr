package sara.damien.app.connection;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import sara.damien.app.Common;
import sara.damien.app.R;
import sara.damien.app.WelcomeActivity;

public class ConnectionTypeActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_type);
    }

    public void adminSignInBtn_Click(View view) {
        Intent i = new Intent(this, WelcomeActivity.class);

        String id = ((EditText)findViewById(R.id.editID)).getText().toString();
        Common.getPrefs().setConnected(true);
        Common.getPrefs().setID(id);
        startActivity(i);
        finish();
    }

    public void linkedInSignInBtn_Click(View view){
        startActivity( new Intent(this, LinkedInConnectionActivity.class));
        finish();
    }

    public void manualSignInBtn_Click(View view){
        startActivity(new Intent(this, RegisterManuallyActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.action_settings)  || super.onOptionsItemSelected(item);
    }

}
