package sara.damien.app;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sara.damien.app.DB.DbHelper;
import sara.damien.app.utils.JSONParser;
import sara.damien.app.utils.Utilities;

interface ProfileListener {
    public void onPictureUpdated();
    public void onContentsDownloaded();
}

public class PrettyProfileMockup extends ActionBarActivity {
    private final int PROFILES_BATCH = 10;
    private final int PICTURES_BATCH = 2;

    ProfilesPagerAdapter pagerAdapter;
    ViewPager viewPager;

    ArrayList<Profile> profiles;
    SharedPreferences prefs;
    private String myID;
    private static final String url = "http://www.golinkr.net";
    DbHelper mDbHelper;
    JSONParser jsonParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pretty_profile_mockup);
        new ProfilesEnumerator().execute();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        myID = prefs.getString("ID","0");
        mDbHelper = new DbHelper(getApplicationContext());
    }

    private void declineTemporarily(){
        profiles.remove(viewPager.getCurrentItem());
        pagerAdapter.notifyDataSetChanged();
    }

    private void likeProfile(){
        CreateMeeting CR = new CreateMeeting();
        Profile currentProfile = profiles.get(viewPager.getCurrentItem());
        CR.ID1 = myID;
        CR.targetProfile = currentProfile;
        CR.execute();
    }

    private class CreateMeeting extends AsyncTask<Void, Void, Boolean>  {
        String ID1;
        Profile targetProfile;
        String IDm;

        @Override
        protected Boolean doInBackground(Void... args) {
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("SELECT_FUNCTION", "createMeeting"));
                params.add(new BasicNameValuePair("ID1", ID1));
                params.add(new BasicNameValuePair("ID2", targetProfile.getID()));
                params.add(new BasicNameValuePair("Subject", targetProfile.getLast_Subject()));
                params.add(new BasicNameValuePair("Message", ""));
                JSONObject json = jsonParser.makeHttpRequest(url, "POST", params);
                if (json.getString("success").equals("1")){
                    IDm = json.getString("ID");
                    mDbHelper.insertLocalProfile(targetProfile);
                    //FIXME: Use the new variant using a Meeting object.
                    // mDbHelper.insertLocalRequestSentMeeting(IDm, ID1, ID2, subject,"0","no message");
                    mDbHelper.getRequestSentMeeting(myID);
                    return true;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                profiles.remove(viewPager.getCurrentItem());
                pagerAdapter.notifyDataSetChanged();
            }
            else {
                Toast.makeText(getApplicationContext(),"Bad connection, try again later...", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void declinePermanently(){
        int currentProfile = viewPager.getCurrentItem();
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> blockedIDs = prefs.getStringSet("blockedIDs", new HashSet<String>());
        blockedIDs.add(profiles.get(currentProfile).getID());
        Log.e("BlockedIDs", blockedIDs.toString());
        editor.putStringSet("blockedIDs", blockedIDs);
        editor.commit();
        profiles.remove(currentProfile);
        pagerAdapter.notifyDataSetChanged();
    }

    private void prefetchProfilesAsync(int position) {
        int rounded_position = PROFILES_BATCH * (position / PROFILES_BATCH);
        new ProfilesDownloader(rounded_position - PROFILES_BATCH, 2 * PROFILES_BATCH).execute();

        for (int offset = -PICTURES_BATCH; offset <= PICTURES_BATCH; offset++) {
            int profile_id = Utilities.wrapIndex(position + offset, profiles.size());
            profiles.get(profile_id).downloadPicture();
        }
    }

    protected void onProfilesEnumerated(ArrayList<Profile> profiles) {
        this.profiles = profiles;

        pagerAdapter = new ProfilesPagerAdapter(getSupportFragmentManager(), profiles);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);
    }

    public class ProfilesPagerAdapter extends FragmentStatePagerAdapter {
        private final ArrayList<Profile> profiles;

        public ProfilesPagerAdapter(FragmentManager fm, ArrayList<Profile> profiles) {
            super(fm);
            this.profiles = profiles;
        }

        @Override
        public Fragment getItem(int position) {
            Profile prof = profiles.get(position);
            prefetchProfilesAsync(position);
            return ProfileFragment.fromProfile(prof);
        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

        @Override
        public int getCount() { //TODO: add a place-holder.
            int count = (profiles != null ? profiles.size() : 0);
            Log.i("PrettyProfileMockup", "getCount() called on ProfilesPagerAdapter, returning " + count);
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null; //TODO: Hide title
        }
    }

    public static class ProfileFragment extends Fragment implements ProfileListener {
        final static String ARGS_PROFILE = "profile";

        private Profile profile;
        private View rootView;

        // This slightly convoluted design relying on setArguments instead of a direct constructor
        // invocation is required to properly persist data.
        // SEE: http://stackoverflow.com/questions/11397099/whats-the-point-of-setarguments
        public static ProfileFragment fromProfile(Profile profile) {
            ProfileFragment fragment = new ProfileFragment();

            Bundle args = new Bundle();
            args.putParcelable(ARGS_PROFILE, profile);
            fragment.setArguments(args);

            return fragment;
        }

        public ProfileFragment() { } //Required by Android

        @Override
        public void onCreate (Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            profile = getArguments().getParcelable(ARGS_PROFILE);
            profile.setParent(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (profile != null)
                profile.setParent(null);
        }

        @Override
        public void onPictureUpdated() {
            getActivity().runOnUiThread(new Runnable() {
                @Override public void run() { updatePictureView(); }
            });
        }

        private void updatePictureView() {
            Log.i("ProfileFragment", "Reloading picture for " + profile);
            ImageView pictureView = (ImageView) rootView.findViewById(R.id.picture_view);
            pictureView.setImageBitmap(profile.getPicture());
        }

        @Override
        public void onContentsDownloaded() {
            getActivity().runOnUiThread(new Runnable() {
                @Override public void run() { updateContentViews(); }
            });
        }

        private void updateContentViews() {
            TextView nameView = (TextView) rootView.findViewById(R.id.name_view);
            TextView industryView = (TextView) rootView.findViewById(R.id.industry_view);
            TextView headlineView = (TextView) rootView.findViewById(R.id.headline_view);

            nameView.setText(Utilities.fallback(profile.getName(), "Sample name"));
            industryView.setText(Utilities.fallback(profile.getIndustry(), "Sample industry"));
            headlineView.setText(Utilities.fallback(profile.getLast_Subject(), "Lorem ipsum dolor sit amet, consectetur adipisicing elit, ..."));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            rootView = inflater.inflate(R.layout.fragment_pretty_profile_mockup, container, false);

            onContentsDownloaded();
            onPictureUpdated();

            Button notNowButton = (Button) rootView.findViewById(R.id.notNow);
            notNowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext() , "You won't view this profile until refresh" , Toast.LENGTH_SHORT).show();
                    ((PrettyProfileMockup)getActivity()).declineTemporarily();
                }
            });
            Button likeProfileButton = (Button) rootView.findViewById(R.id.likeProfile);
            likeProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext() , "You liked this profile and he will receive a notification" , Toast.LENGTH_SHORT).show();
                    ((PrettyProfileMockup)getActivity()).likeProfile();
                }
            });
            Button neverEverButton = (Button) rootView.findViewById(R.id.neverEver);
            neverEverButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getActivity().getApplicationContext() , "You won't view this profile ever again" , Toast.LENGTH_SHORT).show();
                    ((PrettyProfileMockup)getActivity()).declinePermanently();
                }
            });

            return rootView;
        }
    }

    private class ProfilesEnumerator extends AsyncTask<Void, Void, Void>  {
        ArrayList<Profile> profiles;

        @Override
        protected Void doInBackground(Void... none) {
            profiles = LinkrAPI.findNeighbours();
            Set<String> blockedIDs = prefs.getStringSet("blockedIDs", new HashSet<String>());
            for (int indexProfile = 0 ; indexProfile < profiles.size() ; indexProfile ++){
                if (blockedIDs.contains(profiles.get(indexProfile).getID())){
                    profiles.remove(indexProfile);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void none) {
            onProfilesEnumerated(profiles);
        }
    }

    private class ProfilesDownloader extends AsyncTask<Void, Void, Void>  { //TODO: Synchronization
        int first, count;

        public ProfilesDownloader(int first, int max_count) {
            this.first = first;
            this.count = Math.min(max_count, profiles.size());
        }

        @Override
        protected Void doInBackground(Void... args) {
            Set<Profile> toDownload = new HashSet<Profile>(count);

            for (int rel_offset = 0; rel_offset <= count; rel_offset++) {
                int abs_offset = Utilities.wrapIndex(first + rel_offset, profiles.size());
                toDownload.add(profiles.get(abs_offset));
            }

            Log.i("ProfilesDownloader", "Filling in " + count + " profiles from " + first);
            LinkrAPI.fillInProfiles(toDownload);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }
    }
}
