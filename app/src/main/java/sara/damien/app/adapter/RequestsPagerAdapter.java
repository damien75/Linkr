package sara.damien.app.adapter;

/**
 * Created by Damien on 26/02/2014.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sara.damien.app.requests.DebateMeetingFragment;
import sara.damien.app.requests.RequestsReceivedFragment;
import sara.damien.app.requests.RequestsSentFragment;

public class RequestsPagerAdapter extends FragmentPagerAdapter {
    Fragment[] fragments;

    public RequestsPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new Fragment[] {
            new RequestsReceivedFragment(),
            new DebateMeetingFragment(),
            new RequestsSentFragment()
        };
    }

    @Override
    public Fragment getItem(int index) {
        return (index < fragments.length ? fragments[index] : null);
    }

    @Override
    public int getCount() { return 3; }
}
