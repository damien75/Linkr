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

public class TabsPagerAdapter extends FragmentPagerAdapter {
    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new RequestsReceivedFragment();
            case 1:
                return new DebateMeetingFragment();
            case 2:
                return new RequestsSentFragment();
        }

        return null;
    }

    @Override
    public int getCount() { return 3; }
}
