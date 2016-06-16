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

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class controlWidgetProvider extends AppWidgetProvider {

    public static class ClockUpdateService extends Service {
        private static final String ACTION_UPDATE =
                "tv.piratemedia.lightcontroler.clock.action.UPDATE";
        private SharedPreferences prefs;

        private final static IntentFilter intentFilter;

        static {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            intentFilter.addAction(ACTION_UPDATE);
        }

        /**
         * BroadcastReceiver receiving the updates.
         */
        private final BroadcastReceiver clockChangedReceiver = new
                BroadcastReceiver() {
                    /**
                     * {@inheritDoc}
                     */
                    public void onReceive(Context context, Intent intent) {
                        Log.d("wear", "Update Clock");
                        Intent updateIntent = new Intent();
                        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                        updateIntent.putExtra("action", "update");
                        context.sendBroadcast(updateIntent);
                    }
                };

        /**
         * {@inheritDoc}
         */
        public IBinder onBind(Intent intent) {
            return null;
        }
        /**
         * {@inheritDoc}
         */
        public void onCreate() {
            super.onCreate();
            prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            registerReceiver(clockChangedReceiver, intentFilter);
        }
        /**
         * {@inheritDoc}
         */
        public void onDestroy() {
            super.onDestroy();

            unregisterReceiver(clockChangedReceiver);
        }
        /**
         * {@inheritDoc}
         */
        public void onStart(Intent intent, int startId) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTION_UPDATE)) {
                    Intent updateIntent = new Intent();
                    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    this.sendBroadcast(updateIntent);
                }
            }
        }
    }

    private static final int LIGHT_ON = 0;
    private static final int LIGHT_OFF = 1;

    private controlCommands Controller;
    private static AppWidgetManager aWM;
    private static ComponentName thisWidget;
    private Map<Integer, Integer> HeightList = new HashMap<Integer, Integer>();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        thisWidget = new ComponentName(context,
                controlWidgetProvider.class);

        updateUI(context, appWidgetManager);

        Intent i = new Intent(context, ClockUpdateService.class);
        i.setAction(notificationService.START_SERVICE);
        context.startService(i);
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        updateUI(context, AppWidgetManager.getInstance(context));
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    public void onDisabled(Context context) {
        super.onDisabled(context);

        context.stopService(new Intent(context,
                ClockUpdateService.class));
    }
    /**
     * {@inheritDoc}
     */
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Intent i = new Intent(context, ClockUpdateService.class);
        i.setAction(notificationService.START_SERVICE);
        context.startService(i);
    }

    public void updateUI(Context context, AppWidgetManager appWidgetManager) {
        thisWidget = new ComponentName(context,
                controlWidgetProvider.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

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

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.control_widget_init);

            if(prefs.getInt("widget_" + widgetId + "_type", 0) == 0) {
                remoteViews.setTextViewText(R.id.headzone1, prefs.getString("pref_zone1", context.getString(R.string.Zone1)));
                remoteViews.setTextViewText(R.id.headzone2, prefs.getString("pref_zone2", context.getString(R.string.Zone2)));
                remoteViews.setTextViewText(R.id.headzone3, prefs.getString("pref_zone3", context.getString(R.string.Zone3)));
                remoteViews.setTextViewText(R.id.headzone4, prefs.getString("pref_zone4", context.getString(R.string.Zone4)));

                remoteViews.setOnClickPendingIntent(R.id.ig,createPendingIntent(0,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i1,createPendingIntent(1,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i2,createPendingIntent(2,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i3,createPendingIntent(3,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i4,createPendingIntent(4,context,true));

                remoteViews.setOnClickPendingIntent(R.id.og,createPendingIntent(0,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o1,createPendingIntent(1,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o2,createPendingIntent(2,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o3,createPendingIntent(3,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o4,createPendingIntent(4,context,false));
            } else {
                remoteViews.setTextViewText(R.id.headzone1, prefs.getString("pref_zone5", context.getString(R.string.Zone1)));
                remoteViews.setTextViewText(R.id.headzone2, prefs.getString("pref_zone6", context.getString(R.string.Zone2)));
                remoteViews.setTextViewText(R.id.headzone3, prefs.getString("pref_zone7", context.getString(R.string.Zone3)));
                remoteViews.setTextViewText(R.id.headzone4, prefs.getString("pref_zone8", context.getString(R.string.Zone4)));

                remoteViews.setOnClickPendingIntent(R.id.ig,createPendingIntent(9,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i1,createPendingIntent(5,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i2,createPendingIntent(6,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i3,createPendingIntent(7,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i4,createPendingIntent(8,context,true));

                remoteViews.setOnClickPendingIntent(R.id.og,createPendingIntent(9,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o1,createPendingIntent(5,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o2,createPendingIntent(6,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o3,createPendingIntent(7,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o4,createPendingIntent(8,context,false));
            }


            Intent intent = new Intent(context, controlPreferences.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.settings, pendingIntent);

            intent = new Intent(context, controller.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.app, pendingIntent);

            if(HeightList.containsKey(widgetId) && HeightList.get(widgetId) < context.getResources().getDimensionPixelSize(R.dimen.widget_no_clock)) {
                Log.d("widget", "Hide Date Time");
                remoteViews.setViewVisibility(R.id.datetime, View.GONE);
            } else {
                remoteViews.setViewVisibility(R.id.datetime, View.VISIBLE);
            }

            remoteViews.setTextViewText(R.id.timeHour, hourString);
            remoteViews.setTextViewText(R.id.timeMinute, minString);
            remoteViews.setTextViewText(R.id.dateDay, Integer.toString(c.get(Calendar.DAY_OF_MONTH)));
            SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
            String month_name = month_date.format(c.getTime());
            remoteViews.setTextViewText(R.id.dateMonth, month_name);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    public PendingIntent createPendingIntent(int i, Context cont, boolean on) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(cont, controlWidgetProvider.class);
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

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.control_widget_init);
        Log.d("widget", "Get Widget Size");
        HeightList.put(appWidgetId, newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT));
        updateUI(context, appWidgetManager);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.hasExtra("action") && intent.getStringExtra("action").equals("update")) {
            Log.d("widget", "update widget");
            this.updateUI(context, AppWidgetManager.getInstance(context));
        } else {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Controller = new controlCommands(context, null);

            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
                Uri data = intent.getData();
                int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
                int zone = Integer.parseInt(data.getScheme());
                if (buttonId == LIGHT_ON) {
                    Controller.LightsOn(zone);
                } else if (buttonId == LIGHT_OFF) {
                    Controller.LightsOff(zone);
                }
            } else {
                //do nothing
                return;
            }
        }
    }
}
