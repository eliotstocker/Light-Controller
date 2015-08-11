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
import android.widget.RadioGroup;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by eliotstocker on 26/10/14.
 */
public class controlWidgetConfig extends ActionBarActivity {
    private Toolbar mActionBarToolbar;
    private int mAppWidgetId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_widget_config);

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

        RadioGroup rg = (RadioGroup)findViewById(R.id.group);
        Button done = (Button)findViewById(R.id.done);

        Log.d("widgetID", "id: " + mAppWidgetId);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rgbw) {
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_type", 0).commit();
                } else if (checkedId == R.id.white) {
                    Log.d("appWidget", "set as white");
                    prefs.edit().putInt("widget_" + mAppWidgetId + "_type", 1).commit();
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            private static final int LIGHT_ON = 0;
            private static final int LIGHT_OFF = 1;

            @Override
            public void onClick(View v) {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());

                RemoteViews views = new RemoteViews(getBaseContext().getPackageName(),
                        R.layout.control_widget_init);

                if(prefs.getInt("widget_" + mAppWidgetId + "_type", 0) == 0) {
                    views.setTextViewText(R.id.headzone1, prefs.getString("pref_zone1", getBaseContext().getString(R.string.Zone1)));
                    views.setTextViewText(R.id.headzone2, prefs.getString("pref_zone2", getBaseContext().getString(R.string.Zone2)));
                    views.setTextViewText(R.id.headzone3, prefs.getString("pref_zone3", getBaseContext().getString(R.string.Zone3)));
                    views.setTextViewText(R.id.headzone4, prefs.getString("pref_zone4", getBaseContext().getString(R.string.Zone4)));

                    views.setOnClickPendingIntent(R.id.ig,createPendingIntent(0,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i1,createPendingIntent(1,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i2,createPendingIntent(2,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i3,createPendingIntent(3,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i4,createPendingIntent(4,getBaseContext(),true));

                    views.setOnClickPendingIntent(R.id.og,createPendingIntent(0,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o1,createPendingIntent(1,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o2,createPendingIntent(2,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o3,createPendingIntent(3,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o4,createPendingIntent(4,getBaseContext(),false));
                } else {
                    views.setTextViewText(R.id.headzone1, prefs.getString("pref_zone5", getBaseContext().getString(R.string.Zone1)));
                    views.setTextViewText(R.id.headzone2, prefs.getString("pref_zone6", getBaseContext().getString(R.string.Zone2)));
                    views.setTextViewText(R.id.headzone3, prefs.getString("pref_zone7", getBaseContext().getString(R.string.Zone3)));
                    views.setTextViewText(R.id.headzone4, prefs.getString("pref_zone8", getBaseContext().getString(R.string.Zone4)));

                    views.setOnClickPendingIntent(R.id.ig,createPendingIntent(9,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i1,createPendingIntent(5,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i2,createPendingIntent(6,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i3,createPendingIntent(7,getBaseContext(),true));
                    views.setOnClickPendingIntent(R.id.i4,createPendingIntent(8,getBaseContext(),true));

                    views.setOnClickPendingIntent(R.id.og,createPendingIntent(9,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o1,createPendingIntent(5,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o2,createPendingIntent(6,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o3,createPendingIntent(7,getBaseContext(),false));
                    views.setOnClickPendingIntent(R.id.o4,createPendingIntent(8,getBaseContext(),false));
                }


                Intent intent = new Intent(getBaseContext(), controlPreferences.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
                views.setOnClickPendingIntent(R.id.settings, pendingIntent);

                intent = new Intent(getBaseContext(), controller.class);
                pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
                views.setOnClickPendingIntent(R.id.app, pendingIntent);

                views.setViewVisibility(R.id.datetime, View.GONE);

                Calendar c = Calendar.getInstance();
                int min = c.get(Calendar.MINUTE);
                String minString = "00";
                if(min < 10) {
                    minString = "0"+min;
                } else {
                    minString = Integer.toString(min);
                }
                int hour = c.get(Calendar.HOUR_OF_DAY);
                String hourString = "00";
                if(hour < 10) {
                    hourString = "0"+hour;
                } else {
                    hourString = Integer.toString(hour);
                }

                views.setTextViewText(R.id.timeHour, hourString);
                views.setTextViewText(R.id.timeMinute, minString);
                views.setTextViewText(R.id.dateDay, Integer.toString(c.get(Calendar.DAY_OF_MONTH)));
                SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                String month_name = month_date.format(c.getTime());
                views.setTextViewText(R.id.dateMonth, month_name);

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
