package tv.piratemedia.lightcontroler;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by harry on 11/01/15.
 */
public class listenerService extends WearableListenerService {

    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d("wearlistener","Recevied message from handheld " + messageEvent.getPath());
        switch(messageEvent.getPath()) {
            case "/zones":
                //recieved zone list, cache locally
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
        //Intent intent = new Intent( this, MainActivity.class );
        //intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        //startActivity( intent );
    }
}
