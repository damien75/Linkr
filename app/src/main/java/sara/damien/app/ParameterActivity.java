package sara.damien.app;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;

//import android.support.v4.app.Fragment;

public class ParameterActivity extends ListActivity {
    ArrayList<HashMap<String,String>> ParameterList;
    private static final String TAG_MENU_ITEM = "success";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameter);

        /*if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }*/
        HashMap<String,String> map = new HashMap<String, String>();
        map.put(TAG_MENU_ITEM,"Edit Profile");
        map.put(TAG_MENU_ITEM,"Requests");
        map.put(TAG_MENU_ITEM,"Share");
        ParameterList.add(map);
        runOnUiThread(new Runnable() {
            public void run() {
                ListAdapter adapter = new SimpleAdapter(
                        ParameterActivity.this, ParameterList,
                        R.layout.list_itemparameter,
                        new String[]{TAG_MENU_ITEM},
                        new int[]{R.id.menu_item});
                setListAdapter(adapter);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.parameter, menu);
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

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_parameter, container, false);
            return rootView;
        }
    }
*/
}
