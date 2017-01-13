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
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.astuetz.PagerSlidingTabStrip;
import com.devadvance.circularseekbar.CircularSeekBar;
import com.heinrichreimersoftware.materialdrawer.DrawerFrameLayout;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;
import com.heinrichreimersoftware.materialdrawer.structure.DrawerProfile;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import tv.piratemedia.lightcontroler.api.ControlProviders;
import tv.piratemedia.lightcontroler.wear.DataLayerListenerService;
import tv.piratemedia.lightcontroler.pebble.pebbleSender;


public class controller extends ActionBarActivity {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    final UUID appUuid = UUID.fromString("1d6c7f01-d948-42a6-aa4e-b2084210ebbc");

    private static controlCommands Controller;
    private static boolean micStarted = false;
    private static boolean candleMode = false;
    private static Context ctx;
    public SaveState appState = null;
    private static SharedPreferences prefs;

    private Toolbar mActionBarToolbar;
    private PagerSlidingTabStrip tabs;
    private PopupWindow mMenu;
    private View MenuView;

    public MyHandler mHandler = null;
    private utils Utils;
    private boolean gotDevice = false;

    private String DeviceMac = "";

    private DrawerFrameLayout drawer;

    private pebbleSender pSender;

    private Menu optionsMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        drawer = (DrawerFrameLayout) findViewById(R.id.drawer);

        setupApp();

        mHandler = new MyHandler();
        Controller = new controlCommands(this, mHandler);
        ctx = this;
        Intent i = new Intent(this, notificationService.class);
        i.setAction(notificationService.START_SERVICE);
        this.startService(i);
        if(Build.VERSION.SDK_INT == 22) {
            //getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            drawer.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        drawer.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        appState = new SaveState(this);
        Utils = new utils(this);
        new DataLayerListenerService();

        //Call Pebble sender, get it to check if pebble is connceted and send up light states
        //Todo need to rethink this as pebble app only gets communication if its running. So maybe detect if app is running, then send? or when app becomes active, send
        if(prefs.getBoolean("pref_pebble", false)){
            Log.d("Controller", "Pebble pref is active, lets start the app up");
            new pebbleSender(ctx).initialConnect(Controller);
            new pebbleSender(ctx).sendZoneNames();
        }else{
            //TODO add this functionality so the broadcast listener for pebble stops (pebbleReceiver) so we dont have it running in background listening for things
            Log.d("Controller", "Pebble prefs is inactive. Lets shut down the listener service so its not running anymore");
        }

    }

    class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case controlCommands.DISCOVERED_DEVICE:
                    String[] DeviceInfo = (String[])msg.obj;
                    String Mac = DeviceInfo[1];
                    String IP = DeviceInfo[0];
                    newDeviceFound(IP, Mac);
                    break;
                case controlCommands.LIST_WIFI_NETWORKS:
                    String NetworkString = (String)msg.obj;
                    String[] Networks = NetworkString.split("\\n\\r");
                    listWifiNetworks(Networks);
            }
        }
    }

    private void listWifiNetworks(final String[] Networks) {
        String[] ShowNetworks = new String[Networks.length - 4];
        for(int i = 2; i < Networks.length - 2; i++) {
            String[] NetworkInfo = Networks[i].split(",");
            ShowNetworks[i - 2] = NetworkInfo[1]+" - "+NetworkInfo[4]+"%";
        }

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setHint("Password");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final Context _this = this;
        new MaterialDialog.Builder(this)
                .title("Controller Wifi Networks")
                .theme(Theme.DARK)
                .items(ShowNetworks)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        final String[] NetworkInfo = Networks[which + 2].split(",");
                        if (NetworkInfo[3].equals("NONE")) {
                            Controller.setWifiNetwork(NetworkInfo[1]);
                        } else {
                            new MaterialDialog.Builder(_this)
                                    .title("Password For: " + NetworkInfo[1])
                                    .theme(Theme.DARK)
                                    .customView(input, false)
                                    .content("Please type the network password")
                                    .positiveText("OK")
                                    .negativeText("Cancel")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            Controller.setWifiNetwork(NetworkInfo[1], "WPA2PSK", "AES", input.getText().toString());
                                        }
                                    })
                                    .build()
                                    .show();
                        }
                        return false;
                    }
                })
                .positiveText("Select")
                .negativeText("Cancel")
                .build()
                .show();
    }

    private void newDeviceFound(final String IP, final String Mac) {
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
                        DeviceMac = Mac;
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
            DeviceMac = Mac;
        } else {
            if (!Devices.contains(Mac + "-known")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("A new Light Control device has been found, Would you like to save/control it?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else {
                if (Devices.getBoolean(Mac + "-known", false)) {
                    gotDevice = true;
                    prefs.edit().putString("pref_light_controller_ip", IP).commit();
                    DeviceMac = Mac;
                }
            }
        }

        if(DeviceMac.toLowerCase().equals(Utils.GetWifiMac().replace(":","").toLowerCase())) {
            optionsMenu.findItem(R.id.action_wifi_setup).setVisible(true);
        } else {
            optionsMenu.findItem(R.id.action_wifi_setup).setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gotDevice = false;
        if(!Utils.isWifiConnection()) {

        }
        attemptDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Controller.killUDPC();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private void attemptDiscovery() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("pref_disable_auto_find", false)) {
            Controller.discover();
        }

        /* Code to try and unregister pebble stuff if its unselcted in settings menu. not quite sure where to put this though, what reloads after a settings change?
         ComponentName pebblereceiver = new ComponentName(ctx,pebbleReceiver.class);
        int status = ctx.getPackageManager().getComponentEnabledSetting(pebblereceiver);
        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Log.d("Package", "pebbl receiver is enabled");
        } else if(status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
            Log.d("Package", "pebbl receiver is disabled");
        }
        Log.d("Packge", "Test log"); */
        //start timer here for no discovery (try 3 times)
    }

    private void setupApp() {

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(!prefs.getBoolean("rgbw_enabled", false) && !prefs.getBoolean("white_enabled", false)) {
            askControlType();
        } else {

            final ViewPager pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(new ControllerPager(getSupportFragmentManager(), this, this));

            tabs = (PagerSlidingTabStrip) findViewById(R.id.pager_title_strip);
            tabs.setViewPager(pager);

            if(!prefs.getBoolean("navigation_tabs", false)) {
                drawer.setProfile(
                        new DrawerProfile()
                                .setAvatar(getResources().getDrawable(R.drawable.icon))
                                .setBackground(getResources().getDrawable(R.drawable.drawer_profile_background))
                                .setName("Light Controller")
                );

                mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
                mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

                pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        mActionBarToolbar.setTitle(pager.getAdapter().getPageTitle(position));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                this.setTitle(pager.getAdapter().getPageTitle(0));
                mActionBarToolbar.setTitle(pager.getAdapter().getPageTitle(0));
                tabs.setVisibility(View.GONE);

                for(int i = 0; i < pager.getAdapter().getCount(); i++) {
                    if(i == 5) {
                        drawer.addDivider();
                    }
                    drawer.addItem(new DrawerItem()
                            .setTextMode(DrawerItem.SINGLE_LINE)
                            .setTextPrimary(pager.getAdapter().getPageTitle(i).toString())
                            .setOnItemClickListener(new DrawerItem.OnItemClickListener() {
                                @Override
                                public void onClick(DrawerItem drawerItem, int i, int i2) {
                                    int item = i2;
                                    if(item > 4) {
                                        item--;
                                    }
                                    pager.setCurrentItem(item, true);
                                    drawer.closeDrawer();
                                }
                            }));
                }

                final PackageManager pm = getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory("tv.piratemedia.lightcontroller.plugin");
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
                if(resolveInfos.size() > 0) {
                    drawer.addDivider();
                    for (final ResolveInfo info : resolveInfos) {
                        drawer.addItem(new DrawerItem()
                                .setTextMode(DrawerItem.SINGLE_LINE)
                                .setTextPrimary(info.loadLabel(pm).toString())
                                .setImage(info.loadIcon(pm))
                                .setOnItemClickListener(new DrawerItem.OnItemClickListener() {
                                    @Override
                                    public void onClick(DrawerItem drawerItem, int i, int i2) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        intent.addCategory("tv.piratemedia.lightcontroller.plugin");
                                        intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                                        startActivity(intent);
                                        drawer.closeDrawer();
                                    }
                                }));
                    }
                }
                drawer.addDivider();
                drawer.addItem(new DrawerItem()
                        .setTextMode(DrawerItem.SINGLE_LINE)
                        .setTextPrimary(getResources().getString(R.string.action_settings))
                        .setOnItemClickListener(new DrawerItem.OnItemClickListener() {
                            @Override
                            public void onClick(DrawerItem drawerItem, int i, int i2) {
                                Intent intent = new Intent(getApplicationContext(), controlPreferences.class);
                                startActivity(intent);
                                finish();
                                drawer.closeDrawer();
                            }
                        }));
            } else {
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }
    }

    private void askControlType() {
        String[] Options = new String[3];
        Options[0] = "White Bulbs Only";
        Options[1] = "RGBW Bulbs Only";
        Options[2] = "Both White and RGBW Bulbs";

        final Activity _this = this;

        new MaterialDialog.Builder(this)
                .title("Which bulbs do you have?")
                .theme(Theme.LIGHT)
                .items(Options)
                .cancelable(false)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                prefs.edit().putBoolean("white_enabled", true).apply();
                                break;
                            case 1:
                                prefs.edit().putBoolean("rgbw_enabled", true).apply();
                                break;
                            case 2:
                                prefs.edit().putBoolean("white_enabled", true).apply();
                                prefs.edit().putBoolean("rgbw_enabled", true).apply();
                                break;
                        }
                        setupApp();
                        return false;
                    }
                })
                .build()
                .show();
    }

    private void setActionbarColor(int c) {
        mActionBarToolbar.setBackgroundColor(c);
        try {
            tabs.setBackgroundColor(c);
        } catch(Exception e) {
            //do nothing
        }

        float[] hsv = new float[3];
        Color.colorToHSV(c, hsv);
        hsv[2] *= 0.8f; // value component
        c = Color.HSVToColor(hsv);

        drawer.setStatusBarBackgroundColor(c);

        //little hack to make the statusbar background redraw when called from another thread
        drawer.openDrawer();
        drawer.closeDrawer();
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
        if(prefs.getBoolean("navigation_tabs", false)) {
            getMenuInflater().inflate(R.menu.controller, menu);
        } else {
            getMenuInflater().inflate(R.menu.controller_alt, menu);
        }

        if(prefs.getBoolean("rgbw_enabled", false) && prefs.getBoolean("white_enabled", false)) {
            menu.findItem(R.id.action_global_on).setVisible(true);
            menu.findItem(R.id.action_global_off).setVisible(true);
        } else {
            menu.findItem(R.id.action_global_on).setVisible(false);
            menu.findItem(R.id.action_global_off).setVisible(false);
        }

        optionsMenu = menu;
        return true;
    }

    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, controlPreferences.class);
            startActivity(intent);
            finish();
            return true;
        } else if(id == R.id.action_global_on) {
            Controller.globalOn();
        } else if(id == R.id.action_global_off) {
            Controller.globalOff();
        } else if(id == android.R.id.home) {
            drawer.openDrawer();
        } else if(id == R.id.action_wifi_setup) {
            Controller.getWifiNetworks();
        } else if(id == R.id.action_zone_setup) {
            Intent intent = new Intent(this, zoneSetupWizard.class);
            startActivity(intent);
            return true;
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
    public static class ControllerPager extends FragmentPagerAdapter {
        Fragment G1 = null;
        Fragment G2 = null;
        Fragment z1 = null;
        Fragment z2 = null;
        Fragment z3 = null;
        Fragment z4 = null;
        Fragment z5 = null;
        Fragment z6 = null;
        Fragment z7 = null;
        Fragment z8 = null;
        private controller mThis;
        private SharedPreferences prefs;
        private Context context;

        public ControllerPager(FragmentManager fm, controller t, Context context) {
            super(fm);
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            mThis = t;
            this.context = context;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {
            if(!prefs.getBoolean("rgbw_enabled", false)) {
                i += 5;
            }
            switch(i) {
                case 0:
                    if(G1 == null) {
                        G1 = RGBWFragment.newInstance(0);
                    }
                    return G1;
                case 1:
                    if(z1 == null) {
                        z1 = RGBWFragment.newInstance(1);
                    }
                    return z1;
                case 2:
                    if(z2 == null) {
                        z2 = RGBWFragment.newInstance(2);
                    }
                    return z2;
                case 3:
                    if(z3 == null) {
                        z3 = RGBWFragment.newInstance(3);
                    }
                    return z3;
                case 4:
                    if(z4 == null) {
                        z4 = RGBWFragment.newInstance(4);
                    }
                    return z4;
                case 5:
                    if(G2 == null) {
                        G2 = WhiteFragment.newInstance(0);
                    }
                    return G2;
                case 6:
                    if(z5 == null) {
                        z5 = WhiteFragment.newInstance(1);
                    }
                    return z5;
                case 7:
                    if(z6 == null) {
                        z6 = WhiteFragment.newInstance(2);
                    }
                    return z6;
                case 8:
                    if(z7 == null) {
                        z7 = WhiteFragment.newInstance(3);
                    }
                    return z7;
                case 9:
                    if(z8 == null) {
                        z8 = WhiteFragment.newInstance(4);
                    }
                    return z8;
                default:
                    if(G1 == null) {
                        G1 = RGBWFragment.newInstance(0);
                    }
                    return G1;
            }
        }

        public void setupZone(int zone) {
            Intent i = new Intent();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName(context.getPackageName(), zoneSetup.class.getCanonicalName()));
            if(!prefs.getBoolean("rgbw_enabled", false)) {
                zone += 5;
            }
            int z = zone;
            if(z > 4) {
                intent.putExtra("type", "white");
                z -= 5;
            } else {
                intent.putExtra("type", "color");
            }
            intent.putExtra("zone", z);
            intent.putExtra("index", zone);
            context.startActivity(intent);
        }

        public int getIndex(String type, int Zone) {
            switch(type) {
                case "color":
                    return Zone;
                case "white":
                    int r = Zone - 4;
                    if(r > 4) {
                        r = 0;
                    }
                    return r;
                default:
                    return -1;
            }
        }

        @Override
        public int getCount() {
            int count = 0;
            if(prefs.getBoolean("rgbw_enabled", false)) {
                count += 5;
            }
            if(prefs.getBoolean("white_enabled", false)) {
                count += 5;
            }
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(!prefs.getBoolean("rgbw_enabled", false)) {
                position += 5;
            }
            switch(position) {
                case 0: return "All Color";
                case 1: return prefs.getString("pref_zone1", "Zone 1");
                case 2: return prefs.getString("pref_zone2", "Zone 2");
                case 3: return prefs.getString("pref_zone3", "Zone 3");
                case 4: return prefs.getString("pref_zone4", "Zone 4");
                case 5: return "All White";
                case 6: return prefs.getString("pref_zone5", "White 1");
                case 7: return prefs.getString("pref_zone6", "White 2");
                case 8: return prefs.getString("pref_zone7", "White 3");
                case 9: return prefs.getString("pref_zone8", "White 4");
            }
            return "unknown";
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            int savedColor = mThis.appState.getColor(position);
            if(savedColor < 0) {
                mThis.setActionbarColor(savedColor);
            } else {
                mThis.setActionbarColor(mThis.getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    public static class RGBWFragment extends Fragment {

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
        public static RGBWFragment newInstance(int sectionNumber) {
            RGBWFragment fragment = new RGBWFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public RGBWFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            if(!recreateView) {
                final View rootView = inflater.inflate(R.layout.rgbw_control, container, false);

                SeekBar brightness = (SeekBar) rootView.findViewById(R.id.brightness);
                Button on = (Button) rootView.findViewById(R.id.on);
                Button off = (Button) rootView.findViewById(R.id.off);
                Button disco = (Button) rootView.findViewById(R.id.disco);
                Button dplus = (Button) rootView.findViewById(R.id.dplus);
                Button dminus = (Button) rootView.findViewById(R.id.dminus);
                Button white = (Button) rootView.findViewById(R.id.white);
                Button night = (Button) rootView.findViewById(R.id.night);
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

                //Return State
                //io.setChecked(((controller)getActivity()).appState.getOnOff(getArguments().getInt(ARG_SECTION_NUMBER)));
                brightness.setProgress((int) (((controller)getActivity()).appState.getBrightness(getArguments().getInt(ARG_SECTION_NUMBER)) * 100f));
                int savedColor = ((controller)getActivity()).appState.getColor(getArguments().getInt(ARG_SECTION_NUMBER));
                if(savedColor < 0) {
                    color.setColor(savedColor);
                    ((controller) getActivity()).setActionbarColor(savedColor);
                }

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
                            Controller.setColor(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER), i);
                            ((controller) getActivity()).setActionbarColor(i);
                            /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                            io.setChecked(true);*/
                        }
                    }
                });

                brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Controller.setBrightness(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER), (float)progress / 100f);
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Controller.finishBrightness(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER), (float)seekBar.getProgress() / 100f);
                    }
                });

                on.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.LightsOn(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER));
                    }
                });
                off.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.LightsOff(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER));
                    }
                });
                /*io.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER));
                        } else {
                            Controller.LightsOff(getArguments().getInt(ARG_SECTION_NUMBER));
                        }
                    }
                });*/

                disco.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.toggleDiscoMode(getArguments().getInt(ARG_SECTION_NUMBER));
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
                    }
                });

                dplus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.discoModeFaster();
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
                    }
                });

                dminus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.discoModeSlower();
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
                    }
                });

                white.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.setToWhite(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER));
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
                        ((controller) getActivity()).setActionbarColor(Color.parseColor("#FFef6c00"));
                    }
                });

                night.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.setToNight(ControlProviders.ZONE_TYPE_COLOR, getArguments().getInt(ARG_SECTION_NUMBER));
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
                try {
                    ((ViewGroup)cacheView.getParent()).removeView(cacheView);
                } catch(Exception e) {

                }
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
    public static class WhiteFragment extends Fragment {

        public boolean recreateView = false;
        private View cacheView = null;
        private boolean disabled = true;
        private int BrightnessCache = 0;
        private int WarmthCache = 0;
        private boolean brightnessTouching = false;
        private boolean warmthTouching = false;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static WhiteFragment newInstance(int sectionNumber) {
            WhiteFragment fragment = new WhiteFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public WhiteFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if(!recreateView) {
                final View rootView = inflater.inflate(R.layout.white_control, container, false);

                final CircularSeekBar brightness = (CircularSeekBar) rootView.findViewById(R.id.brightness);
                CircularSeekBar warmth = (CircularSeekBar) rootView.findViewById(R.id.warmth);
                final TextView brightnessvalue = (TextView) rootView.findViewById(R.id.brightnessvalue);
                final TextView warmthvalue = (TextView) rootView.findViewById(R.id.warmthvalue);
                Button on = (Button) rootView.findViewById(R.id.on);
                Button off = (Button) rootView.findViewById(R.id.off);
                Button full = (Button) rootView.findViewById(R.id.full);
                Button night = (Button) rootView.findViewById(R.id.night);

                //Return State
                //io.setChecked(((controller)getActivity()).appState.getOnOff(getArguments().getInt(ARG_SECTION_NUMBER)));
                brightness.setProgress((int) (((controller)getActivity()).appState.getBrightness(getArguments().getInt(ARG_SECTION_NUMBER)) * 100));
                int savedColor = ((controller)getActivity()).appState.getColor(getArguments().getInt(ARG_SECTION_NUMBER));
                if(savedColor < 0) {
                    ((controller) getActivity()).setActionbarColor(getResources().getColor(R.color.colorPrimary));
                }

                brightness.setProgress(10);
                brightness.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                        if(brightnessTouching) {
                            if (progress < 11) {
                                brightnessvalue.setText("" + (progress - 10));
                            } else {
                                brightnessvalue.setText("+" + (progress - 10));
                            }
                            if (progress > BrightnessCache) {
                                for (int i = progress; i > BrightnessCache; i--) {
                                    Controller.setBrightnessUpOne(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                                }
                            } else if (progress < BrightnessCache) {
                                for (int i = progress; i < BrightnessCache; i++) {
                                    Controller.setBrightnessDownOne(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                                }
                            }

                            BrightnessCache = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(CircularSeekBar seekBar) {
                        brightnessTouching = false;
                        brightnessvalue.setAlpha(0.0f);
                        int brightness = seekBar.getProgress() - 10;
                        /*if(brightness != 0) {
                            seekBar.setProgress(8);
                            Controller.setBrightnessJog(getArguments().getInt(ARG_SECTION_NUMBER), brightness);
                        }*/
                        seekBar.setProgress(10);
                    }

                    @Override
                    public void onStartTrackingTouch(CircularSeekBar seekBar) {
                        brightnessvalue.setAlpha(1.0f);
                        BrightnessCache = 11;
                        //Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER));
                        brightnessTouching = true;
                    }
                });

                warmth.setProgress(10);
                warmth.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                        if(warmthTouching) {
                            progress = 20 - progress;
                            if (progress < 11) {
                                warmthvalue.setText("" + (progress - 10));
                            } else {
                                warmthvalue.setText("+" + (progress - 10));
                            }
                            if (progress > WarmthCache) {
                                for (int i = progress; i > WarmthCache; i--) {
                                    Controller.setWarmthUpOne(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                                }
                            } else if (progress < WarmthCache) {
                                for (int i = progress; i < WarmthCache; i++) {
                                    Controller.setWarmthDownOne(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                                }
                            }

                            WarmthCache = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(CircularSeekBar seekBar) {
                        warmthTouching = false;
                        warmthvalue.setAlpha(0.0f);
                        int warmth = seekBar.getProgress() - 10;
                        /*if(warmth != 0) {
                            seekBar.setProgress(8);
                            Controller.setWarmthJog(getArguments().getInt(ARG_SECTION_NUMBER), warmth);
                        }*/
                        seekBar.setProgress(10);
                    }

                    @Override
                    public void onStartTrackingTouch(CircularSeekBar seekBar) {
                        warmthvalue.setAlpha(1.0f);
                        WarmthCache = 11;
                        //Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER));
                        warmthTouching = true;
                    }
                });

                on.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.LightsOn(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                    }
                });
                off.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.LightsOff(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                    }
                });
                /*io.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            Controller.LightsOn(getArguments().getInt(ARG_SECTION_NUMBER));
                        } else {
                            Controller.LightsOff(getArguments().getInt(ARG_SECTION_NUMBER));
                        }
                    }
                });*/

                full.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.setToFull(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
                    }
                });

                night.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Controller.setToNight(ControlProviders.ZONE_TYPE_WHITE, getArguments().getInt(ARG_SECTION_NUMBER));
                        /*ToggleButton io = (ToggleButton) rootView.findViewById(R.id.onoff);
                        io.setChecked(true);*/
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
                try {
                    ((ViewGroup) cacheView.getParent()).removeView(cacheView);
                } catch(Exception e) {

                }
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
