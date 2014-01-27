package tv.piratemedia.lightcontroler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by eliotstocker on 02/01/2014.
 */
public class controlWidgetProvider extends AppWidgetProvider {

    private static final int LIGHT_ON = 0;
    private static final int LIGHT_OFF = 1;

    private controlCommands Controller;
    private RemoteViews remoteViews;
    private AppWidgetManager aWM;
    private ComponentName thisWidget;
    private Handler mHandler  = new Handler();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateNames(context, appWidgetManager);
    }

    public void updateNames(Context context, AppWidgetManager appWidgetManager) {
        thisWidget = new ComponentName(context,
                controlWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.control_widget_init);

            aWM = appWidgetManager;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

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

            setTime();

            Intent intent = new Intent(context, controlPreferences.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.settings, pendingIntent);

            intent = new Intent(context, controller.class);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.app, pendingIntent);

            /*mHandler.removeCallbacks(mUpdateTask);
            mHandler.postDelayed(mUpdateTask, 100);*/

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

    public void setTime() {
        Calendar c = Calendar.getInstance();
        int min = c.get(Calendar.MINUTE);
        String minString;
        if(min < 10) {
            minString = "0"+min;
        } else {
            minString = Integer.toString(min);
        }
        remoteViews.setTextViewText(R.id.timeHour,Integer.toString(c.get(Calendar.HOUR_OF_DAY)));
        remoteViews.setTextViewText(R.id.timeMinute,minString);
        remoteViews.setTextViewText(R.id.dateDay, Integer.toString(c.get(Calendar.DAY_OF_MONTH)));
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String month_name = month_date.format(c.getTime());
        remoteViews.setTextViewText(R.id.dateMonth,month_name);
    }

    final Runnable mUpdateTask = new Runnable()
    {
        public void run()
        {
            setTime();
            aWM.updateAppWidget(thisWidget, remoteViews);
            mHandler.postDelayed(mUpdateTask, 1000);

        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Controller = new controlCommands(context);

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
            // Don't fall-through to updating the widget.  The Intent
            // was something unrelated or that our super class took
            // care of.
            return;
        }
        return;
    }
}
