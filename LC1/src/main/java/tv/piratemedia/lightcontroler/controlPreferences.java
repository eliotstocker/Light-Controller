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
import android.os.Handler;
import android.os.Message;
import android.preference.*;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import tv.piratemedia.lightcontroler.api.ControlProviders;
import tv.piratemedia.lightcontroler.api.Provider;

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
        mActionBarToolbar.setNavigationIcon(R.drawable.ic_ab_back_material);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlPreferences.this.finish();
            }
        });
    }

    public void resetPrefs() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PrefsFragment newFragment = new PrefsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.prefs_layout, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static class PrefsFragment extends PreferenceFragment {

        private static final Pattern PARTIAl_IP_ADDRESS =
                Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){3}"+
                        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){1}$");
        private Provider[] availableProviders;
        private Handler h;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.control_preferences);

            Log.d("Providers", "List Providers");
            availableProviders = ControlProviders.listProviders(getActivity());
            ListPreference providers = (ListPreference) findPreference("pref_selected_provider");
            if(availableProviders.length < 1) {
                getPreferenceScreen().removePreference(findPreference("cat_providers"));
            } else {
                String[] entries = new String[availableProviders.length + 1];
                String[] values = new String[availableProviders.length + 1];
                entries[0] = "Built in MiiLight Control";
                values[0] = "";
                int i = 1;
                for(Provider p : availableProviders) {
                    entries[i] = p.Name;
                    values[i] = p.Package;
                }

                providers.setEntries(entries);
                providers.setEntryValues(values);

                //Log.d("providers", providers.getValue());
                if(providers.getValue() == null) {
                    providers.setValueIndex(0);
                } else {
                    try {
                        Provider provider = ControlProviders.getProvider(providers.getValue(), getActivity());
                        PreferenceCategory mCategory = (PreferenceCategory) findPreference("cat_unit_prefs");
                        Preference autodiscovery = findPreference("pref_disable_auto_find");
                        Preference hubip = findPreference("pref_light_controller_ip");
                        Preference hubport = findPreference("pref_light_controller_port");

                        try {
                            if (!provider.CanDisableDiscovery && !provider.CanSetHubIP && !provider.CanSetHubPort) {
                                getPreferenceScreen().removePreference(mCategory);
                            } else {
                                if (!provider.CanDisableDiscovery) {
                                    mCategory.removePreference(autodiscovery);
                                }
                                if (!provider.CanSetHubIP) {
                                    mCategory.removePreference(hubip);
                                }
                                if (!provider.CanSetHubPort) {
                                    mCategory.removePreference(hubport);
                                }
                            }
                        } catch (Exception e) {

                        }
                    } catch(NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                providers.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if(!newValue.equals("")) {
                            Log.d("Providers", "Getting Provider: " + newValue);
                            Provider provider = ControlProviders.getProvider((String) newValue, getActivity());
                            ControlProviders.sendCommand(provider, "Select", getActivity());

                            ((controlPreferences) getActivity()).resetPrefs();
                        }
                        return true;
                    }
                });
            }

            EditTextPreference controllerIP = (EditTextPreference) findPreference("pref_light_controller_ip");

            CheckBoxPreference rgbEnable = (CheckBoxPreference) findPreference("rgbw_enabled");
            final EditTextPreference zone1 = (EditTextPreference) findPreference("pref_zone1");
            final EditTextPreference zone2 = (EditTextPreference) findPreference("pref_zone2");
            final EditTextPreference zone3 = (EditTextPreference) findPreference("pref_zone3");
            final EditTextPreference zone4 = (EditTextPreference) findPreference("pref_zone4");

            CheckBoxPreference whiteEnable = (CheckBoxPreference) findPreference("white_enabled");
            final EditTextPreference zone5 = (EditTextPreference) findPreference("pref_zone5");
            final EditTextPreference zone6 = (EditTextPreference) findPreference("pref_zone6");
            final EditTextPreference zone7 = (EditTextPreference) findPreference("pref_zone7");
            final EditTextPreference zone8 = (EditTextPreference) findPreference("pref_zone8");

            if(!rgbEnable.isChecked()) {
                zone1.setEnabled(false);
                zone2.setEnabled(false);
                zone3.setEnabled(false);
                zone4.setEnabled(false);
            }

            rgbEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                        zone1.setEnabled((Boolean) newValue);
                        zone2.setEnabled((Boolean) newValue);
                        zone3.setEnabled((Boolean) newValue);
                        zone4.setEnabled((Boolean) newValue);
                    return true;
                }
            });

            if(!whiteEnable.isChecked()) {
                zone5.setEnabled(false);
                zone6.setEnabled(false);
                zone7.setEnabled(false);
                zone8.setEnabled(false);
            }

            whiteEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    zone5.setEnabled((Boolean) newValue);
                    zone6.setEnabled((Boolean) newValue);
                    zone7.setEnabled((Boolean) newValue);
                    zone8.setEnabled((Boolean) newValue);
                    return true;
                }
            });

            zone1.setSummary(zone1.getText());
            zone2.setSummary(zone2.getText());
            zone3.setSummary(zone3.getText());
            zone4.setSummary(zone4.getText());
            zone5.setSummary(zone5.getText());
            zone6.setSummary(zone6.getText());
            zone7.setSummary(zone7.getText());
            zone8.setSummary(zone8.getText());

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

            zone5.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });
            zone6.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });
            zone7.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary((CharSequence)newValue);
                    return true;
                }
            });
            zone8.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
                            if(utils.validHost(nv)) {
                                return true;
                            }
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

        startActivity(NavUtils.getParentActivityIntent(this));
    }
}
