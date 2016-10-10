package tv.piratemedia.lightcontroler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class ZonesPagerAdapter extends FragmentPagerAdapter {
    private class ControlPage {
        ZoneFragment fragment;
        String title;

        ControlPage (ZoneFragment fragment, String title) {
            this.fragment = fragment;
            this.title = title;
        }
    }

    private ArrayList<ControlPage> pages = new ArrayList<>();

    ZonesPagerAdapter(FragmentManager fm, MainActivity t, SharedPreferences prefs) {
        super(fm);

        if (prefs.getBoolean("rgbw_enabled", true)) {
            pages.add(new ControlPage(ColorZoneFragment.newInstance(0, "All Color"), "All Color"));

            for (int i = 1; i <= 4; i++) {
                if (prefs.getBoolean("pref_zone" + i + "_enabled", true)) {
                    pages.add(new ControlPage(ColorZoneFragment.newInstance(i - 1, prefs.getString("pref_zone" + i, "Zone " + i)), prefs.getString("pref_zone" + i, "Zone " + i)));
                }
            }
        }
        if (prefs.getBoolean("white_enabled", true)) {
            pages.add(new ControlPage(WhiteZoneFragment.newInstance(9, "All White"), "All White"));

            for (int i = 5; i <= 8; i++) {
                if (prefs.getBoolean("pref_zone" + i + "_enabled", true)) {
                    pages.add(new ControlPage(WhiteZoneFragment.newInstance(i - 1, prefs.getString("pref_zone" + i, "Zone " + (i - 4))), prefs.getString("pref_zone" + (i - 4), "Zone " + (i - 4))));
                }
            }
        }
    }

    @Override
    public ZoneFragment getItem(int i) {
        return pages.get(i).fragment;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public CharSequence getPageTitle(int i) {
        if (i < pages.size()) {
            return pages.get(i).title;
        } else {
            return "Unknown";
        }
    }

//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        super.setPrimaryItem(container, position, object);
//    }

    boolean isColor(int i) {
        return pages.get(i).fragment instanceof ColorZoneFragment;
    }
}
