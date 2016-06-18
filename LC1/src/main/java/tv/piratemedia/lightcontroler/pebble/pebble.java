package tv.piratemedia.lightcontroler.pebble;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

/*
created by mrwhale 18/06/2016
This is the services that connects to and listens to a pebble watch. Receives commands and then actions them
 */
public class pebble extends Activity{
    final UUID appUuid = UUID.fromString("1d6c7f01-d948-42a6-aa4e-b2084210ebbc");
    // Create a new dictionary
    PebbleDictionary dict = new PebbleDictionary();
    private PebbleDataReceiver dataReceiver;

    public void onResume() {
        super.onResume();
        Log.d("pebbleapp", "starting onResume in pebble java");
        boolean isConnected = PebbleKit.isWatchConnected(this);
        Log.d("Pebble app", "Pebble " + (isConnected ? "is" : "is not") + " connected!");

        // Create a new receiver to get AppMessages from the C app
        dataReceiver = new PebbleDataReceiver(appUuid) {

            @Override
            public void receiveData(Context context, int transaction_id, PebbleDictionary dict) {
                Log.d("pebble app", dict + " was received by the android app");
                // A new AppMessage was received, tell Pebble
                PebbleKit.sendAckToPebble(context, transaction_id);
            }

        };
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataReceiver);

    }

    protected void onPause(){
        super.onPause();
        unregisterReceiver(dataReceiver);
    }



}
