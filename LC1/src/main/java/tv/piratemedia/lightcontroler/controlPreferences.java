/*
*    Light Controller, to Control wifi LED Lighting
*    Copyright (C) 2014  Eliot Stocker
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.regex.Pattern;

public class controlPreferences extends ActionBarActivity {
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.control_prefs);

        // Display the fragment as the main content.
        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        PrefsFragment mPrefsFragment = new PrefsFragment();
        mFragmentTransaction.add(R.id.prefs_layout, mPrefsFragment);
        mFragmentTransaction.commit();

        if(Build.VERSION.SDK_INT == 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlPreferences.this.finish();
            }
        });
    }

    public static class PrefsFragment extends PreferenceFragment {

        private static final Pattern PARTIAl_IP_ADDRESS =
                Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){3}"+
                        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){1}$");

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.control_preferences);

            EditTextPreference controllerIP = (EditTextPreference) findPreference("pref_light_controller_ip");

            EditTextPreference zone1 = (EditTextPreference) findPreference("pref_zone1");
            EditTextPreference zone2 = (EditTextPreference) findPreference("pref_zone2");
            EditTextPreference zone3 = (EditTextPreference) findPreference("pref_zone3");
            EditTextPreference zone4 = (EditTextPreference) findPreference("pref_zone4");

            zone1.setSummary(zone1.getText());
            zone2.setSummary(zone2.getText());
            zone3.setSummary(zone3.getText());
            zone4.setSummary(zone4.getText());

            zone1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });
            zone2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });
            zone3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });
            zone4.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });

            final Activity app = (controlPreferences)getActivity();

            try {
                controllerIP.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String oldValue = preference.getSharedPreferences().getString(preference.getKey(), "192.168.0.255");
                        String nv = newValue.toString();
                        if (PARTIAl_IP_ADDRESS.matcher(nv).matches()) {
                            return true;
                        } else {
                            Toast toast = Toast.makeText(app.getApplicationContext(), String.format(getResources().getString(R.string.ip_invalid_error),nv,oldValue), Toast.LENGTH_SHORT);
                            toast.show();
                            return false;
                        }
                    }
                });
            } catch(NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(this,controlWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int[] ids = {R.xml.control_appwidget_info};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
    }
}
