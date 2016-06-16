package tv.piratemedia.lightcontroler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;


public class notificationService extends Service {
    private SharedPreferences prefs;
    private static Notification Notif;
    private NotificationManager nm;
    private int Notif_id = 46598;
    private int Notif_id_w = 46588;
    public static final String START_SERVICE = "tv.piratemedia.lightcontroler.service.start";

    private final static IntentFilter intentFilter;

    static {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
    }

    private final BroadcastReceiver prescenceReciever = new
            BroadcastReceiver() {

                private static final int LIGHT_ON = 0;
                private static final int LIGHT_OFF = 1;

                /**
                 * {@inheritDoc}
                 */
                public void onReceive(Context context, Intent intent) {
                    prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                    if(prefs.getBoolean("lockscreen_notification_white", false)) {
                        if(prefs.getBoolean("white_enabled", false)) {
                            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                                RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.control_notification);
                                contentView.setTextViewText(R.id.headzone1, prefs.getString("pref_zone5", context.getString(R.string.Zone1)));
                                contentView.setTextViewText(R.id.headzone2, prefs.getString("pref_zone6", context.getString(R.string.Zone2)));
                                contentView.setTextViewText(R.id.headzone3, prefs.getString("pref_zone7", context.getString(R.string.Zone3)));
                                contentView.setTextViewText(R.id.headzone4, prefs.getString("pref_zone8", context.getString(R.string.Zone4)));

                                contentView.setOnClickPendingIntent(R.id.ig,createPendingIntent(9,context,true));
                                contentView.setOnClickPendingIntent(R.id.i1,createPendingIntent(5,context,true));
                                contentView.setOnClickPendingIntent(R.id.i2,createPendingIntent(6,context,true));
                                contentView.setOnClickPendingIntent(R.id.i3,createPendingIntent(7,context,true));
                                contentView.setOnClickPendingIntent(R.id.i4,createPendingIntent(8,context,true));

                                contentView.setOnClickPendingIntent(R.id.og,createPendingIntent(9,context,false));
                                contentView.setOnClickPendingIntent(R.id.o1,createPendingIntent(5,context,false));
                                contentView.setOnClickPendingIntent(R.id.o2,createPendingIntent(6,context,false));
                                contentView.setOnClickPendingIntent(R.id.o3,createPendingIntent(7,context,false));
                                contentView.setOnClickPendingIntent(R.id.o4,createPendingIntent(8,context,false));

                                Notif = new NotificationCompat.Builder(context)
                                        .setContentTitle("Light Controller")
                                        .setContentText("Control White Lights from here")
                                        .setOngoing(true)
                                        .setSmallIcon(R.drawable.bulb)
                                        .setPriority(NotificationCompat.PRIORITY_MAX)
                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                        .build();
                                Notif.flags |= Notification.FLAG_NO_CLEAR;
                                Notif.contentView = contentView;
                                nm.notify(Notif_id_w, Notif);
                            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                                nm.cancel(Notif_id_w);
                            }
                        }
                    }
                    if(prefs.getBoolean("lockscreen_notification", false)) {
                        if (prefs.getBoolean("rgbw_enabled", false)) {
                            nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                                RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.control_notification);
                                contentView.setTextViewText(R.id.headzone1, prefs.getString("pref_zone1", context.getString(R.string.Zone1)));
                                contentView.setTextViewText(R.id.headzone2, prefs.getString("pref_zone2", context.getString(R.string.Zone2)));
                                contentView.setTextViewText(R.id.headzone3, prefs.getString("pref_zone3", context.getString(R.string.Zone3)));
                                contentView.setTextViewText(R.id.headzone4, prefs.getString("pref_zone4", context.getString(R.string.Zone4)));

                                contentView.setOnClickPendingIntent(R.id.ig, createPendingIntent(0, context, true));
                                contentView.setOnClickPendingIntent(R.id.i1, createPendingIntent(1, context, true));
                                contentView.setOnClickPendingIntent(R.id.i2, createPendingIntent(2, context, true));
                                contentView.setOnClickPendingIntent(R.id.i3, createPendingIntent(3, context, true));
                                contentView.setOnClickPendingIntent(R.id.i4, createPendingIntent(4, context, true));

                                contentView.setOnClickPendingIntent(R.id.og, createPendingIntent(0, context, false));
                                contentView.setOnClickPendingIntent(R.id.o1, createPendingIntent(1, context, false));
                                contentView.setOnClickPendingIntent(R.id.o2, createPendingIntent(2, context, false));
                                contentView.setOnClickPendingIntent(R.id.o3, createPendingIntent(3, context, false));
                                contentView.setOnClickPendingIntent(R.id.o4, createPendingIntent(4, context, false));

                                Notif = new NotificationCompat.Builder(context)
                                        .setContentTitle("Light Controller")
                                        .setContentText("Control RGBW Lights from here")
                                        .setOngoing(true)
                                        .setSmallIcon(R.drawable.bulb)
                                        .setPriority(NotificationCompat.PRIORITY_MAX)
                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                        .build();
                                Notif.flags |= Notification.FLAG_NO_CLEAR;
                                Notif.contentView = contentView;
                                nm.notify(Notif_id, Notif);
                            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                                nm.cancel(Notif_id);
                            }
                        }
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
            };

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(prescenceReciever, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(prescenceReciever);
    }
}
