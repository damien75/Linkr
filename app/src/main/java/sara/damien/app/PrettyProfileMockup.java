package sara.damien.app;

import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pretty_profile_mockup);
        new ProfilesEnumerator().execute();
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
            headlineView.setText(Utilities.fallback(profile.getHeadline(), "Lorem ipsum dolor sit amet, consectetur adipisicing elit, ..."));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            rootView = inflater.inflate(R.layout.fragment_pretty_profile_mockup, container, false);

            onContentsDownloaded();
            onPictureUpdated();

            return rootView;
        }
    }

    private class ProfilesEnumerator extends AsyncTask<Void, Void, Void>  {
        ArrayList<Profile> profiles;

        @Override
        protected Void doInBackground(Void... none) {
            profiles = LinkrAPI.findNeighbours();
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
