package tv.piratemedia.lightcontroler.pebble;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.view.accessibility.AccessibilityEvent;
import java.util.UUID;

import tv.piratemedia.lightcontroler.controller;

/*
created by mrwhale 18/06/2016
This is the services that connects to and listens to a pebble watch. Receives commands and then actions them
 */
public class pebble {
    static final UUID appUuid = UUID.fromString("1d6c7f01-d948-42a6-aa4e-b2084210ebbc");
    private static final int KEY_CMD = 2;

    // Create a new dictionary

    PebbleDictionary dict = new PebbleDictionary();
    //public static PebbleDataReceiver dataReceiver;

    /*
    public void onResume() {
        super.onResume();
        Log.d("pebble app", "starting onResume in pebble java");
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
    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(dataReceiver);
    } */
/*Pebble related activities. Pass through context, and data receiver, so we can then pause it from Controller

 */
    public static void pebble2(Context ctx, PebbleDataReceiver dataReceiver){
        Log.d("pebble app", "starting onResume in pebble java");
        boolean isConnected = PebbleKit.isWatchConnected(ctx);
        Log.d("Pebble app", "Pebble " + (isConnected ? "is" : "is not") + " connected!");
        //TODO probably should put a if statement to check if pebble is conncted/if we are on the right wifi, so we dont do any work we done need to
        // Create a new receiver to get AppMessages from the C app
        dataReceiver = new PebbleDataReceiver(appUuid) {

            @Override
            public void receiveData(Context context, int transaction_id, PebbleDictionary dict) {
                Log.d("pebble app", "pebble java" + dict + " was received by the android app");
                // A new AppMessage was received, tell Pebble
                Long cmdValue = dict.getUnsignedIntegerAsLong(2);
                if(cmdValue != null){
                    int cmd = cmdValue.intValue();
                    Log.d("Pebble app","from pebble " + cmd);
                }
                PebbleKit.sendAckToPebble(context, transaction_id);
            }

        };
        PebbleKit.registerReceivedDataHandler(ctx, dataReceiver);

    }
}
