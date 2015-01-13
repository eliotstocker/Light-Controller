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
        //Intent intent = new Intent( this, MainActivity.class );
        //intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        //startActivity( intent );

        super.onMessageReceived(messageEvent);
        Log.d("wearlistener","Recevied message from handheld " + messageEvent.getPath() );
        int notificationId = 001;
// Build intent for notification content
        Intent viewIntent = new Intent(this, MainActivity.class);
        //viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Control me")
                        .setContentIntent(viewPendingIntent);

// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());

    }
}
