package tv.piratemedia.lightcontroler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class APIReciever extends BroadcastReceiver {
    public static final String REQUEST_API_PERMISSION = "tv.piratemedia.lightcontroler.requestAPIPermission";
    public static final String LIGHT_ON_INTENT = "tv.piratemedia.lightcontroler.LightOn";
    public static final String LIGHT_OFF_INTENT = "tv.piratemedia.lightcontroler.LightOff";
    public static final String LIGHT_COLOR_INTENT = "tv.piratemedia.lightcontroler.LightColor";
    public static final String LIGHT_SET_DEFAULT = "tv.piratemedia.lightcontroler.LightDefault";

    private static final String ACCEPT_APP_INTENT = "tv.piratemedia.lightcontroler.internal.AcceptApp";
    private static final String DENY_APP_INTENT = "tv.piratemedia.lightcontroler.internal.DenyApp";

    public static final String TYPE_WHITE = "white";
    public static final String TYPE_COLOR = "color";

    private int mId = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Set<String> enabled = prefs.getStringSet("enabled_api_apps", new HashSet<String>());
        if(intent.getAction().equals(ACCEPT_APP_INTENT) || intent.getAction().equals(DENY_APP_INTENT)) {
            Iterator<String> it = intent.getExtras().keySet().iterator();
            while(it.hasNext()) {
                Log.d("Intent", "key: "+it.next());
            }
            parseIntentRequest(context, intent);
        } else if(intent.hasExtra("app_id")) {
            String appId = intent.getStringExtra("app_id");
            if(!enabled.contains(appId)) {
                //show popup
                mId = makeIdFromPackage(appId);
                onRequestAPIPermisson(context, appId, intent);
            } else {
                parseIntentRequest(context, intent);
            }
        } else {
            Log.e("Light Controller API","No app id received");
        }
    }

    private void parseIntentRequest(Context context, Intent intent) {
        controlCommands c = new controlCommands(context, null);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        switch (intent.getAction()) {
            case ACCEPT_APP_INTENT:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                Set<String> enabled = prefs.getStringSet("enabled_api_apps", new HashSet<String>());
                String appId = intent.getStringExtra("app_id");
                enabled.add(appId);
                prefs.edit().putStringSet("enabled_api_apps", enabled).commit();
                manager.cancel(intent.getIntExtra("notifID", -1));
                if(!intent.getStringExtra("initialAction").equals(REQUEST_API_PERMISSION)) {
                    intent.setAction(intent.getStringExtra("initialAction"));
                    parseIntentRequest(context, intent);
                }
                break;
            case DENY_APP_INTENT:
                Log.d("intent", "remove notification: "+intent.getIntExtra("notifID", -1));
                manager.cancel(intent.getIntExtra("notifID", -1));
                break;
            case LIGHT_ON_INTENT:
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    Log.d("Lights on ", "Zone: "+intent.getIntExtra("zone", -1));
                    switch (intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOn(0);
                            break;
                        case 1:
                            c.LightsOn(1);
                            break;
                        case 2:
                            c.LightsOn(2);
                            break;
                        case 3:
                            c.LightsOn(3);
                            break;
                        case 4:
                            c.LightsOn(4);
                            break;
                    }
                } else if (intent.getStringExtra("type").equals(TYPE_WHITE)) {
                    switch (intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOn(9);
                            break;
                        case 1:
                            c.LightsOn(5);
                            break;
                        case 2:
                            c.LightsOn(6);
                            break;
                        case 3:
                            c.LightsOn(7);
                            break;
                        case 4:
                            c.LightsOn(8);
                            break;
                    }
                }
                break;
            case LIGHT_OFF_INTENT:
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    switch (intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOff(0);
                            break;
                        case 1:
                            c.LightsOff(1);
                            break;
                        case 2:
                            c.LightsOff(2);
                            break;
                        case 3:
                            c.LightsOff(3);
                            break;
                        case 4:
                            c.LightsOff(4);
                            break;
                    }
                } else if (intent.getStringExtra("type").equals(TYPE_WHITE)) {
                    switch (intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOff(9);
                            break;
                        case 1:
                            c.LightsOff(5);
                            break;
                        case 2:
                            c.LightsOff(6);
                            break;
                        case 3:
                            c.LightsOff(7);
                            break;
                        case 4:
                            c.LightsOff(8);
                            break;
                    }
                }
                break;
            case LIGHT_COLOR_INTENT:
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    int color = intent.getIntExtra("color", -1);
                    if (color != -1) {
                        int zone = intent.getIntExtra("zone", -1);
                        if (zone > -1 && zone < 5) {
                            switch (intent.getIntExtra("zone", -1)) {
                                case 0:
                                    c.LightsOn(0);
                                    break;
                                case 1:
                                    c.LightsOn(1);
                                    break;
                                case 2:
                                    c.LightsOn(2);
                                    break;
                                case 3:
                                    c.LightsOn(3);
                                    break;
                                case 4:
                                    c.LightsOn(4);
                                    break;
                            }
                            c.setColor(zone, color);
                        }
                    }
                }
                break;
            case LIGHT_SET_DEFAULT:
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    int zone = intent.getIntExtra("zone", -1);
                    if (zone > -1 && zone < 5) {
                        c.setToWhite(zone);
                    }
                } else {
                    int zone = intent.getIntExtra("zone", -1);
                }
                break;
        }
    }

    private void onRequestAPIPermisson(Context context, String appID, Intent in) {
        final PackageManager pm = context.getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( appID, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        if(ai != null) {
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

            NotificationCompat.BigTextStyle notiStyle = new
                    NotificationCompat.BigTextStyle();
            notiStyle.setBigContentTitle("Light Control Permission");
            notiStyle.bigText("'"+applicationName+"' is requesting permission to control your lights");

            long[] pattern = {0, 100, 100};

            Drawable d = pm.getApplicationIcon(ai);
            Bitmap bitmapIcon = ((BitmapDrawable)d).getBitmap();

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setLargeIcon(bitmapIcon)
                            .setSmallIcon(R.drawable.bulb)
                            .setColor(context.getResources().getColor(R.color.colorAccent))
                            .setContentTitle("Light Control Permission")
                            .setContentText("'"+applicationName+"' is requesting permission to control your lights")
                            .addAction(android.R.drawable.ic_menu_view, "Accept", AcceptControlRequest(context, in))
                            .addAction(android.R.drawable.ic_delete, "Decline", DeclineControlRequest(context))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setVibrate(pattern)
                            .setStyle(notiStyle);

            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(mId, mBuilder.build());
        }
    }

    public PendingIntent AcceptControlRequest(Context cont, Intent in) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(cont, APIReciever.class);
        launchIntent.setAction(ACCEPT_APP_INTENT);
        launchIntent.putExtras(in.getExtras());
        launchIntent.putExtra("notifID", mId);
        launchIntent.putExtra("initialAction", in.getAction());
        PendingIntent pi = PendingIntent.getBroadcast(cont, 0 /* no requestCode */,
                launchIntent, 0 /* no flags */);
        return pi;
    }

    public PendingIntent DeclineControlRequest(Context cont) {
        Intent launchIntent = new Intent();
        launchIntent.setClass(cont, APIReciever.class);
        launchIntent.setAction(DENY_APP_INTENT);
        launchIntent.putExtra("notifID", mId);
        PendingIntent pi = PendingIntent.getBroadcast(cont, 0 /* no requestCode */,
                launchIntent, 0 /* no flags */);
        return pi;
    }

    private int makeIdFromPackage(String pid) {
        String[] pack = pid.split("\\.");
        int ID = 0;
        for(int i = 0; i < pack.length; i++) {
            ID += Character.getNumericValue(pack[i].charAt(0));
            ID += Character.getNumericValue(pack[i].charAt(1));
        }
        return ID;
    }
}
