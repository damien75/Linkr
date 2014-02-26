package sara.damien.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import sara.damien.app.adapter.NavDrawerListAdapter;
import sara.damien.app.model.NavDrawerItem;


public class WelcomeActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        //GPS Tracker
        GPSTracker gps = new GPSTracker(WelcomeActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            Log.e("Location GPS","latitude = "+String.valueOf(latitude)+" longitude = "+String.valueOf(longitude));
            editor.putFloat("Latitude",(float)latitude);
            editor.putFloat("Longitude",(float)longitude);
            editor.putBoolean("LocationFound",true);
            shareLocation share = new shareLocation();
            share.loc_x=latitude;
            share.loc_y=longitude;
            share.execute();
        }
        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            editor.putBoolean("LocationFound",false);
            gps.showSettingsAlert();
        }
        editor.commit();

        //Drawer Menu
        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        // Find People
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Photos
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1),true,"50+"));
        // Communities, Will add a counter here
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, "22"));
        // Pages
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        // What's hot, We  will add a counter here
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "Bye bye..."));


        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(0);
        }
    }

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                break;
            case 1:
                //fragment = new FindPeopleFragment();
                Intent i1 = new Intent(this,SwipeActivity.class);
                startActivity(i1);
                break;
            case 2:
                //fragment = new PhotosFragment();
                Intent i2 = new Intent(this,Requests2Activity.class);
                startActivity(i2);
                break;
            case 3:
                //fragment = new CommunityFragment();
                Intent i3 = new Intent(this,Requests3Activity.class);
                startActivity(i3);
                break;
            case 4:
                //fragment = new PagesFragment();
                Intent i4 =new Intent(this,TopicActivity.class);
                startActivity(i4);
                break;
            case 5:
                //fragment = new WhatsHotFragment();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("Connected");
                editor.putBoolean("Connected", false);
                editor.commit();
                Intent i5 = new Intent(this,SplashScreenActivity.class);
                startActivity(i5);
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
        /*HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt("position",position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container,fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(navMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.welcome, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void openSearch(){
        Toast.makeText(this,"Search Selected",Toast.LENGTH_LONG).show();
    }

    public void openSettings(){
        Toast.makeText(this,"Settings Selected",Toast.LENGTH_LONG).show();
    }

    public void openTopic (View view){
        Intent intent=new Intent(this,TopicActivity.class);
        startActivity(intent);
    }

    public void openRequests (View view){
        Intent intent=new Intent(this,RequestsActivity.class);
        startActivity(intent);
    }

    public void openProfile (View view){
        Intent intent=new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }

    public class shareLocation extends AsyncTask<Void,Void,Void> {
        private double loc_x;
        private double loc_y;

        @Override
        protected Void doInBackground(Void... args) {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int userID = prefs.getInt("ID",1);
                JSONParser jsonParser = new JSONParser();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("SELECT_FUNCTION","shareLocation"));
                params.add(new BasicNameValuePair("ID", String.valueOf(userID)));
                params.add(new BasicNameValuePair("Loc_X", String.valueOf(loc_x)));
                params.add(new BasicNameValuePair("Loc_Y", String.valueOf(loc_y)));
                JSONObject json = jsonParser.makeHttpRequest("http://www.golinkr.net","POST",params);
                Log.e("shareLocation","location was shared for user with ID "+String.valueOf(userID));

            }
            catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.


     public static class PlaceholderFragment extends android.app.Fragment {

     public PlaceholderFragment() {
     }

     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
     Bundle savedInstanceState) {
     View rootView = inflater.inflate(R.layout.fragment_welcome, container, false);
     return rootView;
     }
     }

     private static abstract class Fragment {
     public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
     Bundle savedInstanceState);
     }*/

}