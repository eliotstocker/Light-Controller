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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v7.app.ActionBarActivity;

/*import com.instabug.library.Instabug;
import com.instabug.library.util.TouchEventDispatcher;
import com.instabug.wrapper.impl.v14.InstabugAnnotationActivity;*/

import com.astuetz.PagerSlidingTabStrip;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class controller extends ActionBarActivity {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private static controlCommands Controller;
    private static boolean micStarted = false;
    private static boolean candleMode = false;
    private static Context ctx;
    private static SharedPreferences prefs;
    private boolean instabug_started = false;

    private Toolbar mActionBarToolbar;
    private PagerSlidingTabStrip tabs;
    private PopupWindow mMenu;
    private View MenuView;

    public MyHandler mHandler = null;

    private boolean gotDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setupApp();

        Controller = new controlCommands(this);
        ctx = this;
        Intent i = new Intent(this, notificationService.class);
        i.setAction(notificationService.START_SERVICE);
        this.startService(i);
        if(Build.VERSION.SDK_INT == 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mHandler = new MyHandler();
    }

    class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case controlCommands.DISCOVERED_DEVICE:
                    String[] DeviceInfo = (String[])msg.obj;
                    String Mac = DeviceInfo[1];
                    String IP = DeviceInfo[0];
                    newDeviceFound(IP, Mac);
            }
        }
    }

    private void newDeviceFound(final String IP, final String Mac) {
        final utils Utils = new utils(this);
        final SharedPreferences Devices = this.getSharedPreferences("devices", MODE_PRIVATE);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Devices.edit().putBoolean(Mac+"-known",true).commit();
                        prefs.edit().putString("pref_light_controller_ip", IP).commit();
                        Devices.edit().putString(Mac+"-online",Utils.getWifiName());
                        gotDevice = true;
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        Devices.edit().putBoolean(Mac+"-known",false).commit();
                        break;
                }
            }
        };

        if(Devices.getAll().size() < 1) {
            prefs.edit().putString("pref_light_controller_ip", IP).commit();
            Devices.edit().putBoolean(Mac+"-known",true);
            Devices.edit().putString(Mac+"-online",Utils.getWifiName());
            gotDevice = true;
        } else {
            if (!Devices.contains(Mac + "-known")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("A new Light Control device has been found, Would you like to save/control it?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else {
                if (Devices.getBoolean(Mac + "-known", false)) {
                    gotDevice = true;
                    prefs.edit().putString("pref_light_controller_ip", IP).commit();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gotDevice = false;
        utils Utils = new utils(this);
        if(!Utils.isWifiConnection()) {

        }
        attemptDiscovery();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void attemptDiscovery() {
        Controller.discover();
        //start timer here for no discovery (try 3 times)
    }

    private void setupApp() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ControllerPager(getSupportFragmentManager()));

        tabs = (PagerSlidingTabStrip) findViewById(R.id.pager_title_strip);
        tabs.setViewPager(pager);

        /*try {
            Instabug.initialize(this.getApplicationContext())
                    .setAnnotationActivityClass(InstabugAnnotationActivity.class)
                    .setShowIntroDialog(true)
                    .setEnableOverflowMenuItem(true)
                    .setCrashReportingEnabled(true)
                    .setTrackUserSteps(true)
                    .setShowIntroDialog(false);
        } catch(IllegalStateException e) {
            //do nothing
        }*/
    }

    /*private class LightControlTabListener implements ActionBar.TabListener {
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
    }*/

    private void setActionbarColor(int c) {
        mActionBarToolbar.setBackgroundColor(c);
        tabs.setIndicatorColor(c);

        float[] hsv = new float[3];
        Color.colorToHSV(c, hsv);
        hsv[2] *= 0.8f; // value component
        c = Color.HSVToColor(hsv);

        if(Build.VERSION.SDK_INT == 21) {
            getWindow().setStatusBarColor(c);
        }
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            /*getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));*/
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        /*outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.controller, menu);
        return true;
    }

    public void popupMenu() {
        MenuView = View.inflate(this, R.layout.menu, null);
        ImageView close = (ImageView) MenuView.findViewById(R.id.close_menu_item);
        TextView settings = (TextView) MenuView.findViewById(R.id.settings_menu_item);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMenu();
                Intent intent = new Intent(getApplicationContext(), controlPreferences.class);
                startActivity(intent);
            }
        });

        MenuView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_corner));

        mMenu = new PopupWindow(MenuView, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mMenu.setContentView(MenuView);
        mMenu.setTouchable(true);
        if(Build.VERSION.SDK_INT == 21) {
            mMenu.setElevation((int)dipToPixels(this, 10));
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        mMenu.setAnimationStyle(R.anim.abc_slide_in_top);
        mMenu.setBackgroundDrawable(new BitmapDrawable());
        mMenu.setFocusable(true);
        mMenu.setOutsideTouchable(false);
        mMenu.showAtLocation(findViewById(R.id.container), 0, size.x - (int)dipToPixels(this, 208), (int)dipToPixels(this, 32));
        mMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                MenuView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_corner));
            }
        });
    }

    public void closeMenu() {
        mMenu.dismiss();
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
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
        }  else if(id == R.id.action_menu) {
            popupMenu();
            return true;
        }/*else if(id == R.id.action_send_feedback) {
            Instabug.getInstance().displayFeedbackDialog();
        } else if(id == R.id.action_report_bug) {
            Instabug.getInstance().startAnnotationActivity(getScreen());
        }*/
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
    public static class ControllerPager extends FragmentPagerAdapter {
        Fragment G = null;
        Fragment z1 = null;
        Fragment z2 = null;
        Fragment z3 = null;
        Fragment z4 = null;

        public ControllerPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            switch(i) {
                case 0:
                    if(G == null) {
                        G = PlaceholderFragment.newInstance(0);
                    }
                    return G;
                case 1:
                    if(z1 == null) {
                        z1 = PlaceholderFragment.newInstance(1);
                    }
                    return z1;
                case 2:
                    if(z2 == null) {
                        z2 = PlaceholderFragment.newInstance(2);
                    }
                    return z2;
                case 3:
                    if(z3 == null) {
                        z3 = PlaceholderFragment.newInstance(3);
                    }
                    return z3;
                case 4:
                    if(z4 == null) {
                        z4 = PlaceholderFragment.newInstance(4);
                    }
                    return z4;
                default:
                    if(G == null) {
                        G = PlaceholderFragment.newInstance(0);
                    }
                    return G;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return "Global";
                case 1: return prefs.getString("pref_zone1", "Zone 1");
                case 2: return prefs.getString("pref_zone2", "Zone 2");
                case 3: return prefs.getString("pref_zone3", "Zone 3");
                case 4: return prefs.getString("pref_zone4", "Zone 4");
            }
            return "unknown";
        }
    }

    public static class PlaceholderFragment extends Fragment {

        public boolean recreateView = false;
        private View cacheView = null;
        private boolean disabled = true;

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
                        if(!disabled) {
                            Controller.setColor(getArguments().getInt(ARG_SECTION_NUMBER), i);
                            ((controller) getActivity()).setActionbarColor(color.getColor());
                            ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                            io.setChecked(true);
                        }
                    }
                });

                brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Controller.setBrightness(getArguments().getInt(ARG_SECTION_NUMBER), progress);
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
                            Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER));
                        } else {
                            Controller.LightsOff(getArguments().getInt(ARG_SECTION_NUMBER));
                        }
                    }
                });

                disco.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.toggleDiscoMode(getArguments().getInt(ARG_SECTION_NUMBER));
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
                        Controller.setToWhite(getArguments().getInt(ARG_SECTION_NUMBER));
                        ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);
                        ((controller) getActivity()).setActionbarColor(Color.parseColor("#ffee58"));
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
                            Controller.startMeasuringVol(getArguments().getInt(ARG_SECTION_NUMBER));
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
                            Controller.startCandleMode(getArguments().getInt(ARG_SECTION_NUMBER));
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
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        disabled = false;
                    }
                }, 500);
                return rootView;
            } else {
                disabled = true;
                ((ViewGroup)cacheView.getParent()).removeView(cacheView);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        disabled = false;
                    }
                }, 500);
                return cacheView;
            }
        }
    }

}
