package tv.piratemedia.lightcontroler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;

/**
 * Created by eliotstocker on 26/10/14.
 */
public class switchWidgetConfig extends ActionBarActivity {
    private Toolbar mActionBarToolbar;
    private int mAppWidgetId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_widget_config);

        if(Build.VERSION.SDK_INT == 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);

        mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        RadioButton z1 = (RadioButton)findViewById(R.id.z1);
        RadioButton z2 = (RadioButton)findViewById(R.id.z2);
        RadioButton z3 = (RadioButton)findViewById(R.id.z3);
        RadioButton z4 = (RadioButton)findViewById(R.id.z4);
        RadioButton z5 = (RadioButton)findViewById(R.id.z5);
        RadioButton z6 = (RadioButton)findViewById(R.id.z6);
        RadioButton z7 = (RadioButton)findViewById(R.id.z7);
        RadioButton z8 = (RadioButton)findViewById(R.id.z8);
        RadioGroup rg = (RadioGroup)findViewById(R.id.group);
        Button done = (Button)findViewById(R.id.done);
        CheckBox show = (CheckBox)findViewById(R.id.show_title);

        z1.setText(prefs.getString("pref_zone1", "Zone 1"));
        z2.setText(prefs.getString("pref_zone2", "Zone 2"));
        z3.setText(prefs.getString("pref_zone3", "Zone 3"));
        z4.setText(prefs.getString("pref_zone4", "Zone 4"));

        z5.setText(prefs.getString("pref_zone5", "Zone 1"));
        z6.setText(prefs.getString("pref_zone6", "Zone 2"));
        z7.setText(prefs.getString("pref_zone7", "Zone 3"));
        z8.setText(prefs.getString("pref_zone8", "Zone 4"));

        Log.d("widgetID", "id: " + mAppWidgetId);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.g) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 0).commit();
                } else if (checkedId == R.id.z1) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 1).commit();
                } else if (checkedId == R.id.z2) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 2).commit();
                } else if (checkedId == R.id.z3) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 3).commit();
                } else if (checkedId == R.id.z4) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 4).commit();
                } else if (checkedId == R.id.z5) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 5).commit();
                } else if (checkedId == R.id.z6) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 6).commit();
                } else if (checkedId == R.id.z7) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 7).commit();
                } else if (checkedId == R.id.z8) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 8).commit();
                } else if(checkedId == R.id.g2) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_zone", 9).commit();
                }
            }
        });

        show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("widget_" + mAppWidgetId + "_title", isChecked).commit();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            private static final int LIGHT_ON = 0;
            private static final int LIGHT_OFF = 1;

            @Override
            public void onClick(View v) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());

                RemoteViews views = new RemoteViews(getBaseContext().getPackageName(),
                        R.layout.switch_widget);

                int ControlZone = prefs.getInt("widget_"+mAppWidgetId+"_zone", 0);
                boolean ShowTitle = prefs.getBoolean("widget_"+mAppWidgetId+"_title", true);

                if(ShowTitle) {
                    String label = "";
                    switch(ControlZone) {
                        case 0:
                            label = getBaseContext().getString(R.string.gloabl);
                            break;
                        case 1:
                            label = prefs.getString("pref_zone1", getBaseContext().getString(R.string.Zone1));
                            break;
                        case 2:
                            label = prefs.getString("pref_zone2", getBaseContext().getString(R.string.Zone2));
                            break;
                        case 3:
                            label = prefs.getString("pref_zone3", getBaseContext().getString(R.string.Zone3));
                            break;
                        case 4:
                            label = prefs.getString("pref_zone4", getBaseContext().getString(R.string.Zone4));
                            break;
                        case 5:
                            label = prefs.getString("pref_zone5", getBaseContext().getString(R.string.Zone1));
                            break;
                        case 6:
                            label = prefs.getString("pref_zone6", getBaseContext().getString(R.string.Zone2));
                            break;
                        case 7:
                            label = prefs.getString("pref_zone7", getBaseContext().getString(R.string.Zone3));
                            break;
                        case 8:
                            label = prefs.getString("pref_zone8", getBaseContext().getString(R.string.Zone4));
                            break;
                        case 9:
                            label = getBaseContext().getString(R.string.gloabl);
                            break;
                    }
                    views.setTextViewText(R.id.zone_label, label);
                    views.setViewVisibility(R.id.zone_label, View.VISIBLE);
                } else {
                    views.setViewVisibility(R.id.zone_label, View.GONE);
                }

                views.setOnClickPendingIntent(R.id.ig,createPendingIntent(ControlZone,getBaseContext(),true));
                views.setOnClickPendingIntent(R.id.og,createPendingIntent(ControlZone,getBaseContext(),false));

                appWidgetManager.updateAppWidget(mAppWidgetId, views);

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }

            public PendingIntent createPendingIntent(int i, Context cont, boolean on) {
                Intent launchIntent = new Intent();
                launchIntent.setClass(cont, switchWidget.class);
                launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
                if(on) {
                    launchIntent.setData(Uri.parse(i + ":" + LIGHT_ON));
                } else {
                    launchIntent.setData(Uri.parse(i + ":" + LIGHT_OFF));
                }
                launchIntent.putExtra("light_zone",i);
                PendingIntent pi = PendingIntent.getBroadcast(cont, 0 /* no requestCode */,
                        launchIntent, 0 /* no flags */);
                return pi;
            }
        });
    }
}
