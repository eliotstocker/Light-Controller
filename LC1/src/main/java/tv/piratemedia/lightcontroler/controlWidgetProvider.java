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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import tv.piratemedia.lightcontroler.api.ControlProviders;

public class controlWidgetProvider extends AppWidgetProvider {

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

                if(prefs.getBoolean("widget_" + widgetId + "_super", false)) {
                    remoteViews.setOnClickPendingIntent(R.id.ig, createSuperPendingIntent(context, true));
                } else {
                    remoteViews.setOnClickPendingIntent(R.id.ig, createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,0, context, true));
                }
                remoteViews.setOnClickPendingIntent(R.id.i1,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,1,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i2,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,2,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i3,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,3,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i4,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,4,context,true));

                if(prefs.getBoolean("widget_" + widgetId + "_super", false)) {
                    remoteViews.setOnClickPendingIntent(R.id.og, createSuperPendingIntent(context, false));
                } else {
                    remoteViews.setOnClickPendingIntent(R.id.og, createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,0, context, false));
                }
                remoteViews.setOnClickPendingIntent(R.id.o1,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,1,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o2,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,2,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o3,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,3,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o4,createPendingIntent(ControlProviders.ZONE_TYPE_COLOR,4,context,false));
            } else {
                remoteViews.setTextViewText(R.id.headzone1, prefs.getString("pref_zone5", context.getString(R.string.Zone1)));
                remoteViews.setTextViewText(R.id.headzone2, prefs.getString("pref_zone6", context.getString(R.string.Zone2)));
                remoteViews.setTextViewText(R.id.headzone3, prefs.getString("pref_zone7", context.getString(R.string.Zone3)));
                remoteViews.setTextViewText(R.id.headzone4, prefs.getString("pref_zone8", context.getString(R.string.Zone4)));

                if(prefs.getBoolean("widget_" + widgetId + "_super", false)) {
                    remoteViews.setOnClickPendingIntent(R.id.ig, createSuperPendingIntent(context, true));
                } else {
                    remoteViews.setOnClickPendingIntent(R.id.ig, createPendingIntent(ControlProviders.ZONE_TYPE_WHITE, 0, context, true));
                }
                remoteViews.setOnClickPendingIntent(R.id.i1,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,1,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i2,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,2,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i3,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,3,context,true));
                remoteViews.setOnClickPendingIntent(R.id.i4,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,4,context,true));

                if(prefs.getBoolean("widget_" + widgetId + "_super", false)) {
                    remoteViews.setOnClickPendingIntent(R.id.og, createSuperPendingIntent(context, false));
                } else {
                    remoteViews.setOnClickPendingIntent(R.id.og, createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,0, context, false));
                }
                remoteViews.setOnClickPendingIntent(R.id.o1,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,1,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o2,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,2,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o3,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,3,context,false));
                remoteViews.setOnClickPendingIntent(R.id.o4,createPendingIntent(ControlProviders.ZONE_TYPE_WHITE,4,context,false));
            }


            Intent intent = new Intent(context, controlPreferences.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.settings, pendingIntent);

            intent = new Intent(context, controller.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.app, pendingIntent);

            if(Build.VERSION.SDK_INT >= 16) {
                int height = appWidgetManager.getAppWidgetOptions(widgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

                if (height < context.getResources().getDimensionPixelSize(R.dimen.widget_no_clock)) {
                    Log.d("widget", "Hide Date Time");
                    remoteViews.setViewVisibility(R.id.datetime, View.GONE);
                } else {
                    remoteViews.setViewVisibility(R.id.datetime, View.VISIBLE);
                }
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

    public PendingIntent createPendingIntent(String Type, int i, Context cont, boolean on) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(cont, controlWidgetProvider.class);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        if(on) {
            launchIntent.setData(Uri.parse(i + ":" + LIGHT_ON));
        } else {
            launchIntent.setData(Uri.parse(i + ":" + LIGHT_OFF));
        }
        launchIntent.putExtra("light_zone",i);
        launchIntent.putExtra("light_type",Type);
        PendingIntent pi = PendingIntent.getBroadcast(cont, 0 /* no requestCode */,
                launchIntent, 0 /* no flags */);
        return pi;
    }

    public PendingIntent createSuperPendingIntent(Context cont, boolean on) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(cont, controlWidgetProvider.class);
        launchIntent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        if(on) {
            launchIntent.setData(Uri.parse("super:" + LIGHT_ON));
        } else {
            launchIntent.setData(Uri.parse("super:" + LIGHT_OFF));
        }
        PendingIntent pi = PendingIntent.getBroadcast(cont, 0 /* no requestCode */,
                launchIntent, 0 /* no flags */);
        return pi;
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.control_widget_init);
        Log.d("widget", "Get Widget Size");
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

            Controller = new controlCommands(context, null);

            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
                Uri data = intent.getData();
                int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
                if(data.getScheme().equals("super")) {
                    if (buttonId == LIGHT_ON) {
                        Controller.globalOn();
                    } else if (buttonId == LIGHT_OFF) {
                        Controller.globalOff();
                    }
                } else {
                    int zone = Integer.parseInt(data.getScheme());
                    if (buttonId == LIGHT_ON) {
                        Controller.LightsOn(intent.getStringExtra("light_type"), zone);
                    } else if (buttonId == LIGHT_OFF) {
                        Controller.LightsOff(intent.getStringExtra("light_type"), zone);
                    }
                }
            } else {
                //do nothing
                return;
            }
        }
    }
}
