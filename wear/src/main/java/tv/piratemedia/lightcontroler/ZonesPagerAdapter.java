package tv.piratemedia.lightcontroler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ZonesPagerAdapter extends FragmentStatePagerAdapter {
    public ZonesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ColorZoneFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(ColorZoneFragment.ARG_OBJECT, i);
        args.putInt(ColorZoneFragment.ARG_COUNT, getCount());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}
