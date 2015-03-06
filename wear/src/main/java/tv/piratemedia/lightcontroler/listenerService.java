package tv.piratemedia.lightcontroler;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
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
        Log.d("wearlistener","Recevied message from handheld " + messageEvent.getPath());
        switch(messageEvent.getPath()) {
            case "/zones":
                //recieved zone list, cache locally
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new ByteArrayInputStream(messageEvent.getData()));
                    List<String> list = (List<String>) ois.readObject();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        ois.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent( this, MainActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                intent.putExtra("updated", true);
                startActivity( intent );
                break;
            case "/wifi-connected":
                int notificationId = 001;
                Intent viewIntent = new Intent(this, MainActivity.class);
                PendingIntent viewPendingIntent =
                        PendingIntent.getActivity(this, 0, viewIntent, 0);

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentTitle("Turn me on")
                                .setContentText("Swipe left to open app")
                                .setContentIntent(viewPendingIntent);

                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(this);

                notificationManager.notify(notificationId, notificationBuilder.build());
                break;
        }
    }
}
