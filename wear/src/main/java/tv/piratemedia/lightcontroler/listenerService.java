package tv.piratemedia.lightcontroler;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by harry on 11/01/15.
 */
public class listenerService extends WearableListenerService {

    public void onMessageReceived(MessageEvent messageEvent) {
        Intent intent = new Intent( this, MainActivity.class );
        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity( intent );

        super.onMessageReceived(messageEvent);
        Log.d("wearlistener","Recevied message from handheld" + messageEvent.getPath() );


    }
}
