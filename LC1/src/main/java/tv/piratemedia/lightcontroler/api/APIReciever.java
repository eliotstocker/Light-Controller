package tv.piratemedia.lightcontroler.api;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import tv.piratemedia.lightcontroler.R;
import tv.piratemedia.lightcontroler.controlCommands;

public class APIReciever extends BroadcastReceiver {
    public static final String REQUEST_API_PERMISSION = "tv.piratemedia.lightcontroler.requestAPIPermission";
    public static final String LIGHT_ON_INTENT = "tv.piratemedia.lightcontroler.LightOn";
    public static final String LIGHT_OFF_INTENT = "tv.piratemedia.lightcontroler.LightOff";
    public static final String LIGHT_COLOR_INTENT = "tv.piratemedia.lightcontroler.LightColor";
    public static final String LIGHT_SET_DEFAULT_INTENT = "tv.piratemedia.lightcontroler.LightDefault";
    public static final String LIGHT_FADE_IN_INTENT = "tv.piratemedia.lightcontroler.LightFadeIn";
    public static final String LIGHT_FADE_OUT_INTENT = "tv.piratemedia.lightcontroler.LightFadeOut";
    public static final String LIGHT_FADE_CANCEL_INTENT = "tv.piratemedia.lightcontroller.LightFadeCancel";


    private static final String ACCEPT_APP_INTENT = "tv.piratemedia.lightcontroler.internal.AcceptApp";
    private static final String DENY_APP_INTENT = "tv.piratemedia.lightcontroler.internal.DenyApp";

    public static final String TYPE_WHITE = "white";
    public static final String TYPE_COLOR = "color";
    public static final String TYPE_SUPER = "super";

    private int mId = 0;

    private ArrayList<AsyncTask<String,String,String>> tasks = new ArrayList<AsyncTask<String,String,String>>();

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Set<String> enabled = prefs.getStringSet("enabled_api_apps", new HashSet<String>());

        if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            Log.d("package", "Package Removed:"+packageName);
            if(enabled.contains(packageName)) {
                enabled.remove(packageName);
                prefs.edit().putStringSet("enabled_api_apps", enabled).apply();
                Log.d("package", "API Permission removed");
            }
            return;
        }

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
        int zone = -1;
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
                context.getContentResolver().notifyChange(Uri.parse("content://tv.piratemedia.lightcontroler.api/permission"), null);
                break;
            case DENY_APP_INTENT:
                Log.d("intent", "remove notification: "+intent.getIntExtra("notifID", -1));
                manager.cancel(intent.getIntExtra("notifID", -1));
                break;
            case LIGHT_ON_INTENT:
                zone = intent.getIntExtra("zone", -1);
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    Log.d("Lights on ", "Zone: "+intent.getIntExtra("zone", -1));
                    cancelCurrentTasks();
                    c.LightsOn(ControlProviders.ZONE_TYPE_COLOR, zone);
                } else if (intent.getStringExtra("type").equals(TYPE_WHITE)) {
                    cancelCurrentTasks();
                    c.LightsOn(ControlProviders.ZONE_TYPE_WHITE, zone);
                } else if (intent.getStringExtra("type").equals(TYPE_SUPER)) {
                    c.globalOn();
                }
                break;
            case LIGHT_OFF_INTENT:
                zone = intent.getIntExtra("zone", -1);
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    cancelCurrentTasks();
                    c.LightsOff(ControlProviders.ZONE_TYPE_COLOR, zone);
                } else if (intent.getStringExtra("type").equals(TYPE_WHITE)) {
                    cancelCurrentTasks();
                    c.LightsOff(ControlProviders.ZONE_TYPE_WHITE, zone);
                } else if (intent.getStringExtra("type").equals(TYPE_SUPER)) {
                    c.globalOff();
                }
                break;
            case LIGHT_COLOR_INTENT:
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    int color = intent.getIntExtra("color", -1);
                    if (color != -1) {
                        zone = intent.getIntExtra("zone", -1);
                        if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                            cancelCurrentTasks();
                            c.setColor(ControlProviders.ZONE_TYPE_COLOR, zone, color);
                        }
                    }
                }
                break;
            case LIGHT_SET_DEFAULT_INTENT:
                zone = intent.getIntExtra("zone", -1);
                if (intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    cancelCurrentTasks();
                    c.setToWhite(ControlProviders.ZONE_TYPE_COLOR, zone);
                } else {
                    cancelCurrentTasks();
                    c.setToFull(ControlProviders.ZONE_TYPE_WHITE, zone);
                }
                break;
            case LIGHT_FADE_IN_INTENT:
                if(intent.getIntExtra("zone", -1) < 0 || intent.getIntExtra("zone", -1) > 9) {
                    break;
                }
                zone = intent.getIntExtra("zone", -1);
                if(intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    int duration = intent.getIntExtra("duration", -1);
                    if(duration <= 900 && duration > -1) {
                        c.setToWhite(ControlProviders.ZONE_TYPE_COLOR, zone);
                        c.setBrightness(ControlProviders.ZONE_TYPE_COLOR, zone, 0);
                    }
                    FadeLights(zone, TYPE_COLOR, true, duration, context);
                } else {
                    int duration = intent.getIntExtra("duration", -1);
                    if(duration <= 900 && duration > 30) {
                        for(int i = 0; i < 10; i++) {
                            c.setBrightnessDownOne(ControlProviders.ZONE_TYPE_WHITE, zone);
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        FadeLights(zone, TYPE_WHITE, true, duration, context);
                    }
                }
                break;
            case LIGHT_FADE_OUT_INTENT:
                zone = intent.getIntExtra("zone", -1);
                if(intent.getIntExtra("zone", -1) < 0 || intent.getIntExtra("zone", -1) > 9) {
                    break;
                }
                if(intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    int duration = intent.getIntExtra("duration", -1);
                    if(duration <= 900 && duration > -1) {
                        c.setToWhite(ControlProviders.ZONE_TYPE_COLOR, zone);
                        c.setBrightness(ControlProviders.ZONE_TYPE_COLOR, zone, 19);
                    }
                    FadeLights(zone, TYPE_COLOR, true, duration, context);
                } else {
                    int duration = intent.getIntExtra("duration", -1);
                    if(duration <= 900 && duration > 30) {
                        c.setToFull(ControlProviders.ZONE_TYPE_WHITE, zone);
                        FadeLights(zone, TYPE_WHITE, true, duration, context);
                    }
                }
                break;
            case LIGHT_FADE_CANCEL_INTENT:
                cancelCurrentTasks();
                break;
        }
    }

    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    private void FadeLights(final int Zone, final String Type, final Boolean in, int duration, Context context) {
        final controlCommands c = new controlCommands(context, null);
        final int interval;
        final int Max;
        if(Type.equals(TYPE_COLOR)) {
            interval = Math.round(duration / 20);
            Max = 20;
        } else {
            interval = Math.round(duration / 10);
            Max = 10;
        }
        final AsyncTask<String, String, String> runFade = new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                int step = 0;
                while(Max > step) {
                    try {
                        Thread.sleep(interval * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (Type.equals(TYPE_COLOR)) {
                        if(in) {
                            c.setBrightness(ControlProviders.ZONE_TYPE_COLOR, Zone, step);
                        } else {
                            int i = 19 - step;
                            c.setBrightness(ControlProviders.ZONE_TYPE_COLOR, Zone, i);
                        }
                    } else {
                        if(in) {
                            c.setBrightnessUpOne(ControlProviders.ZONE_TYPE_WHITE, Zone);
                        } else {
                            c.setBrightnessDownOne(ControlProviders.ZONE_TYPE_WHITE, Zone);
                        }
                    }
                    step++;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        };
        runFade.execute();
        tasks.add(runFade);
    }

    private void cancelCurrentTasks() {
        for(int i = 0; i < tasks.size(); i++) {
            AsyncTask<String, String, String> ast = tasks.get(i);
            ast.cancel(true);
            tasks.remove(i);
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
            final String applicationName = (String) pm.getApplicationLabel(ai);

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
                            .addAction(R.drawable.ic_done_24dp, "Accept", AcceptControlRequest(context, in))
                            .addAction(R.drawable.ic_clear_24dp, "Decline", DeclineControlRequest(context))
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
