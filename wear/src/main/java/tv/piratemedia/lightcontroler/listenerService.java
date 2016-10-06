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

    private class ZoneInfo {
        boolean enabled = true;
        String name = "Zone";
    }

    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
        int notificationId = 11447;
        switch(messageEvent.getPath()) {
            case "/zones":
                // received zone list, cache locally
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new ByteArrayInputStream(messageEvent.getData()));
                    Object readObj = ois.readObject();
                    List<ZoneInfo> list = (List<ZoneInfo>) readObj;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    for (int i = 0; i < list.size(); i++) {
                        prefs.edit()
                            .putString("pref_zone" + i, list.get(i).name)
                            .putBoolean("pref_zone" + i + "_enabled", list.get(i).enabled)
                            .apply();
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

                Intent intent = new Intent();
                intent.setAction("tv.piratemedia.lightcontroler.wear.updated_zones");
                sendBroadcast(intent);

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
