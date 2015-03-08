package tv.piratemedia.lightcontroler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

public class ZonesPagerAdapter extends FragmentStatePagerAdapter {
    public ZonesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        if(i > 4) {
            fragment = new WhiteZoneFragment();
        } else {
            fragment = new ColorZoneFragment();
        }
        Bundle args = new Bundle();

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

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }
    
    public boolean isColor(int position) {
        if(position > 4) {
            return false;
        } else {
            return true;
        }
    }
}
