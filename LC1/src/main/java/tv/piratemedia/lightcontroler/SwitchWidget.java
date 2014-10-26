package tv.piratemedia.lightcontroler;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class SwitchWidget extends AppWidgetProvider {

    private static final int LIGHT_ON = 0;
    private static final int LIGHT_OFF = 1;

    private controlCommands Controller;
    private static RemoteViews remoteViews;
    private static AppWidgetManager aWM;
    private static ComponentName thisWidget;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateNames(context, appWidgetManager);
    }

    public void updateNames(Context context, AppWidgetManager appWidgetManager) {
        thisWidget = new ComponentName(context,
                SwitchWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.large_widget);

            aWM = appWidgetManager;

            remoteViews.setOnClickPendingIntent(R.id.ig,createPendingIntent(0,context,true));
            remoteViews.setOnClickPendingIntent(R.id.og,createPendingIntent(0,context,false));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    public PendingIntent createPendingIntent(int i, Context cont, boolean on) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(cont, SwitchWidget.class);
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
            //do nothing
            return;
        }
    }
}