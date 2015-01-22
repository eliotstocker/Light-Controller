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
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
                        setTime(context);
                        //checkAlarm();
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
                    setTime(this);
                }
            }
        }

        /*public void checkAlarm() {
            String[] Keys = (String[]) prefs.getAll().keySet().toArray();
            Map<String, ?> Prefs = prefs.getAll();
            Calendar c = Calendar.getInstance();
            long t = c.getTimeInMillis() / 1000;
            for(int i = 0; i < Prefs.size(); i++) {
                if(Keys[i].startsWith("light-alarm-days")) {
                    boolean on = prefs.getBoolean(Keys[i].replace("days", "enabled"), false);
                    String days = prefs.getString(Keys[i], null);
                    String[] dayArray = days.split(",");
                    int time = prefs.getInt(Keys[i].replace("days","time"), 0);
                    for(int j = 0; j < dayArray.length; j++) {
                        if(on && dayArray[j].equals(c.get(Calendar.DAY_OF_WEEK))) {
                            int secondOfDay = (c.get(Calendar.HOUR_OF_DAY) * 60 * 60) + (c.get(Calendar.MINUTE) * 60);
                            if(on && time >= secondOfDay && time < secondOfDay + 60) {
                                Log.d("Eliot", "Day Alarm Active now");
                            }
                        }
                    }
                } else if(Keys[i].startsWith("light-alarm-date")) {
                    boolean on = prefs.getBoolean(Keys[i].replace("date", "enabled"), false);
                    long date = prefs.getLong(Keys[i],0);
                    if(on && date >= t && date < (t + 60)) {
                        Log.d("Eliot", "One Time Alarm Active now");
                        prefs.edit().putBoolean(Keys[i].replace("date", "enabled"), false).commit();
                    }
                }
            }
        }*/
    }

    private static final int LIGHT_ON = 0;
    private static final int LIGHT_OFF = 1;

    private controlCommands Controller;
    private static AppWidgetManager aWM;
    private static ComponentName thisWidget;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        thisWidget = new ComponentName(context,
                controlWidgetProvider.class);

        aWM = appWidgetManager;

        updateNames(context, appWidgetManager);

        Intent i = new Intent(context, ClockUpdateService.class);
        i.setAction(notificationService.START_SERVICE);
        context.startService(i);
        setTime(context);
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

    public void updateNames(Context context, AppWidgetManager appWidgetManager) {
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.control_widget_init);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Log.d("appWidget", widgetId + " - type: " + prefs.getInt("widget_" + widgetId + "_type", 0));
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

    public static void setTime(Context ctx) {
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
        try {
            int[] allWidgetIds = aWM.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {
                try {
                    RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(),
                            R.layout.control_widget_init);
                    remoteViews.setTextViewText(R.id.timeHour, hourString);
                    remoteViews.setTextViewText(R.id.timeMinute, minString);
                    remoteViews.setTextViewText(R.id.dateDay, Integer.toString(c.get(Calendar.DAY_OF_MONTH)));
                    SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
                    String month_name = month_date.format(c.getTime());
                    remoteViews.setTextViewText(R.id.dateMonth, month_name);

                    aWM.updateAppWidget(widgetId, remoteViews);
                } catch(NullPointerException e) {
                    //dont need to do anything really
                }
            }
        } catch(NullPointerException e) {
            //dont need to do anything really
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

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
