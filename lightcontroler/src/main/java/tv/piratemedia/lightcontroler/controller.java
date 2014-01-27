package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.IOException;

public class controller extends Activity implements ActionBar.OnNavigationListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private static controlCommands Controller;
    private static boolean micStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[] {
                                getString(R.string.gloabl),
                                prefs.getString("pref_zone1", getString(R.string.Zone1)),
                                prefs.getString("pref_zone2", getString(R.string.Zone2)),
                                prefs.getString("pref_zone3", getString(R.string.Zone3)),
                                prefs.getString("pref_zone4", getString(R.string.Zone4)),
                        }),
                this);
        Controller = new controlCommands(this);
        //Controller.startMeasuringVol();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.controller, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, controlPreferences.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.global_control, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            SeekBar brightness = (SeekBar) rootView.findViewById(R.id.brightness);
            Switch io = (Switch) rootView.findViewById(R.id.onoff);
            Button disco = (Button) rootView.findViewById(R.id.disco);
            Button dplus = (Button) rootView.findViewById(R.id.dplus);
            Button dminus = (Button) rootView.findViewById(R.id.dminus);
            Button white = (Button) rootView.findViewById(R.id.white);
            final ColorPicker color = (ColorPicker) rootView.findViewById(R.id.color);
            SeekBar tolerance = (SeekBar) rootView.findViewById(R.id.mictolerance);
            final Button toggleMic = (Button) rootView.findViewById(R.id.mic);

            if(micStarted) {
                toggleMic.setText("Stop Listening");
            } else {
                toggleMic.setText("Start Listening");
            }

            color.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {
                    Controller.setColor(getArguments().getInt(ARG_SECTION_NUMBER) - 1,i);
                    color.setOldCenterColor(i);
                    Switch io = (Switch) rootView.findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Controller.setBrightness(getArguments().getInt(ARG_SECTION_NUMBER) - 1,progress);
                    Switch io = (Switch) rootView.findViewById(R.id.onoff);
                    io.setChecked(true);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            io.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                    } else {
                        Controller.LightsOff(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                    }
                }
            });

            disco.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Controller.toggleDiscoMode(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                    Switch io = (Switch) rootView.findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            dplus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Controller.discoModeFaster();
                    Switch io = (Switch) rootView.findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            dminus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Controller.discoModeSlower();
                    Switch io = (Switch) rootView.findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            white.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Controller.setToWhite(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                    Switch io = (Switch) rootView.findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            tolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Controller.tolerance[0] = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            toggleMic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!micStarted) {
                        toggleMic.setText("Stop Listening");
                        Controller.startMeasuringVol(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                        micStarted = true;
                    } else {
                        toggleMic.setText("Start Listening");
                        Controller.stopMeasuringVol();
                        micStarted  = false;
                    }
                }
            });

            return rootView;
        }
    }

}
