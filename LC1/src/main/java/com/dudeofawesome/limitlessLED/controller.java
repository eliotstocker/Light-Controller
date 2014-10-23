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
package com.dudeofawesome.limitlessLED;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.instabug.library.Instabug;
import com.instabug.library.util.TouchEventDispatcher;
import com.instabug.wrapper.impl.v14.InstabugAnnotationActivity;

import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class controller extends Activity {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private static controlCommands Controller;
    private static boolean micStarted = false;
    private static boolean candleMode = false;
    private static boolean disableColorChange = false;
    private Handler handler = new Handler();
    private static Context ctx;
    private boolean instabug_started = false;
    private TouchEventDispatcher dispatcher = new TouchEventDispatcher();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setupApp();

        Controller = new controlCommands(this);
        ctx = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setupApp();
        Controller.recreateUDPC();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        dispatcher.dispatchTouchEvent(this,ev);
        return super.dispatchTouchEvent(ev);
    }

    private void setupApp() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.removeAllTabs();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        actionBar.addTab(actionBar.newTab().setText(R.string.global).setTabListener(new LightControlTabListener(this, "global")));
        actionBar.addTab(actionBar.newTab().setText(prefs.getString("pref_zone1", getString(R.string.Zone1))).setTabListener(new LightControlTabListener(this, "zone1")));
        actionBar.addTab(actionBar.newTab().setText(prefs.getString("pref_zone2", getString(R.string.Zone2))).setTabListener(new LightControlTabListener(this, "zone2")));
        actionBar.addTab(actionBar.newTab().setText(prefs.getString("pref_zone3", getString(R.string.Zone3))).setTabListener(new LightControlTabListener(this, "zone3")));
        actionBar.addTab(actionBar.newTab().setText(prefs.getString("pref_zone4", getString(R.string.Zone4))).setTabListener(new LightControlTabListener(this, "zone4")));

        try {
            Instabug.initialize(this.getApplicationContext())
                    .setAnnotationActivityClass(InstabugAnnotationActivity.class)
                    .setShowIntroDialog(true)
                    .setEnableOverflowMenuItem(true)
                    .setCrashReportingEnabled(true)
                    .setTrackUserSteps(true)
                    .setShowIntroDialog(false);
        } catch(IllegalStateException e) {
            //do nothing
        }
    }

    private class LightControlTabListener implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;

        public LightControlTabListener(Activity activity, String tag) {
            mActivity = activity;
            mTag = tag;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = PlaceholderFragment.newInstance(tab.getPosition() + 1);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                disableColorChange = true;
                ft.attach(mFragment);
                //set timer to re-enable color changer
                handler.postDelayed(runnable, 400);
            }
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.detach(mFragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            disableColorChange = false;
        }
    };


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
        } else if(id == R.id.action_send_feedback) {
            Instabug.getInstance().displayFeedbackDialog();
        } else if(id == R.id.action_report_bug) {
            Instabug.getInstance().startAnnotationActivity(getScreen());
        }
        return super.onOptionsItemSelected(item);
    }

    private File getScreen() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String mPath = Environment.getExternalStorageDirectory().toString() + "/bug_report_"+ts+".jpg";

// create bitmap screen capture
        Bitmap bitmap;
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        File imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();
            return imageFile;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public boolean recreateView = false;
        private View cacheView = null;

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
            if(!recreateView) {
                final View rootView = inflater.inflate(R.layout.global_control, container, false);

                SeekBar brightness = (SeekBar) rootView.findViewById(R.id.brightness);
                ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                Button disco = (Button) rootView.findViewById(R.id.disco);
                Button dplus = (Button) rootView.findViewById(R.id.dplus);
                Button dminus = (Button) rootView.findViewById(R.id.dminus);
                Button white = (Button) rootView.findViewById(R.id.white);
                final ColorPicker color = (ColorPicker) rootView.findViewById(R.id.color);
                SeekBar tolerance = (SeekBar) rootView.findViewById(R.id.mictolerance);
                final Button toggleMic = (Button) rootView.findViewById(R.id.mic);
                final Button toggleCandle = (Button) rootView.findViewById(R.id.candle_mode);

                Spinner modeSpinner = (Spinner) rootView.findViewById(R.id.movement_modes);
                final LinearLayout modeContainer = (LinearLayout) rootView.findViewById(R.id.modes_container);

                ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(ctx,
                        R.array.modes_array, android.R.layout.simple_spinner_item);
                modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                modeSpinner.setAdapter(modeAdapter);

                if (micStarted) {
                    toggleMic.setText("Stop Listening");
                } else {
                    toggleMic.setText("Start Listening");
                }

                if (candleMode) {
                    toggleCandle.setText("Stop Candle Mode");
                } else {
                    toggleCandle.setText("Start Candle Mode");
                }

                modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            for (int i = 0; i < modeContainer.getChildCount(); i++) {
                                modeContainer.getChildAt(i).setVisibility(View.GONE);
                            }
                            modeContainer.getChildAt(position).setVisibility(View.VISIBLE);
                        } catch(NullPointerException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                color.setShowOldCenterColor(false);
                color.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int i) {
                        if(!disableColorChange) {
                            LinearLayout rl = (LinearLayout) rootView.findViewById(R.id.mainLayout);
                            ActionBar bar = getActivity().getActionBar();
                            float[] hsv = new float[3];
                            int rgb = Color.BLACK;
                            Color.colorToHSV(i, hsv);
                            hsv[2] = 0.8f;
                            rgb = Color.HSVToColor(hsv);
                            rl.setBackgroundColor(rgb);
                            hsv[0] += 180;
                            hsv[2] = 0.9f;
                            if (hsv[0] > 360) hsv[0] -= 360;
                            rgb = Color.HSVToColor(hsv);
                            bar.setBackgroundDrawable(new ColorDrawable(rgb));
                            Controller.setColor(getArguments().getInt(ARG_SECTION_NUMBER) - 1, i);
                            ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                            io.setChecked(true);
                        }
                    }
                });

                brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Controller.setBrightness(getArguments().getInt(ARG_SECTION_NUMBER) - 1, progress);
                        ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
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
                        if (isChecked) {
//                            LinearLayout rl = (LinearLayout) rootView.findViewById(R.id.mainLayout);
//                            ActionBar bar = getActivity().getActionBar();
//                            rl.setBackgroundColor(Color.rgb(100, 100, 100));
//                            bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(200, 200, 200)));
                            Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                        } else {
                            LinearLayout rl = (LinearLayout) rootView.findViewById(R.id.mainLayout);
                            ActionBar bar = getActivity().getActionBar();
                            rl.setBackgroundColor(Color.BLACK);
                            bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(50, 50, 50)));
                            Controller.LightsOff(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                        }
                    }
                });

                disco.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.toggleDiscoMode(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                        ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);
                    }
                });

                dplus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.discoModeFaster();
                        ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);
                    }
                });

                dminus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.discoModeSlower();
                        ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);
                    }
                });

                white.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout rl = (LinearLayout) rootView.findViewById(R.id.mainLayout);
                        ActionBar bar = getActivity().getActionBar();
                        rl.setBackgroundColor(Color.rgb(100, 100, 100));
                        bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(200, 200, 200)));
                        Controller.setToWhite(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                        ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
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
                        if (!micStarted) {
                            toggleMic.setText("Stop Listening");
                            Controller.startMeasuringVol(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                            micStarted = true;
                        } else {
                            toggleMic.setText("Start Listening");
                            Controller.stopMeasuringVol();
                            micStarted = false;
                        }
                    }
                });

                toggleCandle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!candleMode) {
                            toggleCandle.setText("Stop Candle Mode");
                            Controller.startCandleMode(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                            candleMode = true;
                        } else {
                            toggleCandle.setText("Start Candle Mode");
                            Controller.stopCandleMode();
                            candleMode = false;
                        }
                    }
                });

                recreateView = true;
                cacheView = rootView;
                return rootView;
            } else {
                return cacheView;
            }
        }
    }
}
