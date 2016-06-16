package tv.piratemedia.lightcontroler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class switchWidget extends AppWidgetProvider {

    private static final int LIGHT_ON = 0;
    private static final int LIGHT_OFF = 1;

    private controlCommands Controller;
    private static RemoteViews remoteViews;
    private static AppWidgetManager aWM;
    private static ComponentName thisWidget;
    private int ControlZone;
    private boolean ShowTitle;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateNames(context, appWidgetManager);
    }

    public void updateNames(Context context, AppWidgetManager appWidgetManager) {
        thisWidget = new ComponentName(context,
                switchWidget.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int widgetId : allWidgetIds) {
            ControlZone = prefs.getInt("widget_"+widgetId+"_zone", 0);
            ShowTitle = prefs.getBoolean("widget_"+widgetId+"_title", true);

            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.switch_widget);

            aWM = appWidgetManager;

            if(ShowTitle) {
                String label = "";
                switch(ControlZone) {
                    case 0:
                        label = context.getString(R.string.gloabl);
                        break;
                    case 1:
                        label = prefs.getString("pref_zone1", context.getString(R.string.Zone1));
                        break;
                    case 2:
                        label = prefs.getString("pref_zone2", context.getString(R.string.Zone2));
                        break;
                    case 3:
                        label = prefs.getString("pref_zone3", context.getString(R.string.Zone3));
                        break;
                    case 4:
                        label = prefs.getString("pref_zone4", context.getString(R.string.Zone4));
                        break;
                    case 5:
                        label = prefs.getString("pref_zone5", context.getString(R.string.Zone1));
                        break;
                    case 6:
                        label = prefs.getString("pref_zone6", context.getString(R.string.Zone2));
                        break;
                    case 7:
                        label = prefs.getString("pref_zone7", context.getString(R.string.Zone3));
                        break;
                    case 8:
                        label = prefs.getString("pref_zone8", context.getString(R.string.Zone4));
                        break;
                    case 9:
                        label = context.getString(R.string.gloabl);
                        break;
                }
                remoteViews.setTextViewText(R.id.zone_label, label);
                remoteViews.setViewVisibility(R.id.zone_label, View.VISIBLE);
            } else {
                remoteViews.setViewVisibility(R.id.zone_label, View.GONE);
            }

            remoteViews.setOnClickPendingIntent(R.id.ig,createPendingIntent(ControlZone,context,true));
            remoteViews.setOnClickPendingIntent(R.id.og,createPendingIntent(ControlZone,context,false));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
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
