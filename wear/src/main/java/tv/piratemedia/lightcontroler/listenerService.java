package tv.piratemedia.lightcontroler;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.List;

public class listenerService extends WearableListenerService {

    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        int notificationId = 11447;
        switch(messageEvent.getPath()) {
            case "/zones":
                //recieved zone list, cache locally
                boolean changes = false;
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new ByteArrayInputStream(messageEvent.getData()));
                    List<String> list = (List<String>) ois.readObject();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    if(!prefs.getString("pref_zone0", "").equals(list.get(0)) ||
                            !prefs.getString("pref_zone1", "").equals(list.get(1)) ||
                            !prefs.getString("pref_zone2", "").equals(list.get(2)) ||
                            !prefs.getString("pref_zone3", "").equals(list.get(3)) ||
                            !prefs.getString("pref_zone4", "").equals(list.get(4)) ||
                            !prefs.getString("pref_zone5", "").equals(list.get(5)) ||
                            !prefs.getString("pref_zone6", "").equals(list.get(6)) ||
                            !prefs.getString("pref_zone7", "").equals(list.get(7)) ||
                            !prefs.getString("pref_zone8", "").equals(list.get(8)) ||
                            !prefs.getString("pref_zone9", "").equals(list.get(9))) {
                        changes = true;
                    }
                    
                    if(changes) {
                        prefs.edit().putString("pref_zone0", list.get(0))
                                .putString("pref_zone1", list.get(1))
                                .putString("pref_zone2", list.get(2))
                                .putString("pref_zone3", list.get(3))
                                .putString("pref_zone4", list.get(4))
                                .putString("pref_zone5", list.get(5))
                                .putString("pref_zone6", list.get(6))
                                .putString("pref_zone7", list.get(7))
                                .putString("pref_zone8", list.get(8))
                                .putString("pref_zone9", list.get(9)).apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(changes) {
                    Intent intent = new Intent();
                    intent.setAction("tv.piratemedia.lightcontroler.wear.updated_zones");
                    sendBroadcast(intent);
                }
                break;
            case "/wifi-connected":
                Intent viewIntent = new Intent(this, MainActivity.class);
                PendingIntent viewPendingIntent =
                        PendingIntent.getActivity(this, 0, viewIntent, 0);

                Bitmap background = BitmapFactory.decodeResource(getResources(),
                        R.drawable.drawer_profile_background);

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Light Controller Connected")
                                .setContentText("Slide left and tap open to open Light Controller")
                                .setSubText("Slide left and tap open to open Light Controller")
                                .setTicker("Slide left and tap open to open Light Controller")
                                .setLargeIcon(background)
                                .setFullScreenIntent(viewPendingIntent, true)
                                .setOngoing(true)
                                .setContentIntent(viewPendingIntent);

                Notification notif = notificationBuilder.build();
                notif.flags = NotificationCompat.FLAG_ONGOING_EVENT;

                notificationManager.notify(notificationId, notif);
                break;
            case "/wifi-disconnected":
                notificationManager.cancel(notificationId);
        }
    }
}
