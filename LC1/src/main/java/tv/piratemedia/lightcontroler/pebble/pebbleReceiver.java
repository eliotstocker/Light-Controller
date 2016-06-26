package tv.piratemedia.lightcontroler.pebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import static com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE;
//import static com.getpebble.android.kit.Constants.APP_UUID;
import static tv.piratemedia.lightcontroler.Constants.WatchUUID;
import org.json.JSONException;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;
import tv.piratemedia.lightcontroler.controlCommands;
import tv.piratemedia.lightcontroler.controller;

import java.util.UUID;
/*
created by mrwhale 18/06/2016
This is the class that connects to and listens to a pebble watch. Receives commands and then actions them
Commands come in the form of a pebble dictionary, which is a tuple (key/value pairs) of data sent by the watch (because
the watch app is written in c) Once received, we extract the data by asking the tuple for the key value (in this case KEY_ZONE,
same as the variable in the watch app) and it gives us back the value, which is the zone number (0-9) Once we have the Zone,
 we send that to a control commands instance which handles switching the lights

 We also have to ask the controlcommands instance for the current state of the light, because there is no
 "toggle" function, we ask what it is, then toggle to the opposite. This way for now because the pebble
 can only do so much in its UI and only has 1 action button. I guess we could change it so long press turns off, and short press turns on? maybe
 //todo have a look at implemeting longpress for off and short press for on
 */
public class pebbleReceiver extends BroadcastReceiver {

    // todo add in a setting option to "enable" pebble. Then we can use this to check if they actually want to be calling this method
    // TODO modify wear wifi option to be "wearable" to include pebble too
    // todo add the ability to send data to the watch.Need to send Zone data to the watch so it can display it
    //TODO probably should put a if statement to check if pebble is conncted/if we are on the right wifi, so we dont do any work we done need to
    //TODO throw error if teither of these dont exist and tell pebble?

    //TODO put some more logic around getting zone value to verify its an int 0-9
    private static final int KEY_CMD = 3;
    private static final int KEY_ZONE = 2;
    controller mCont = new controller();
    controlCommands contCmd;

    static String TAG = "Pebble Receiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        contCmd = new controlCommands(context, mCont.mHandler);
        Log.d("pebble app R", "on recieve");
        if (intent.getAction().equals(INTENT_APP_RECEIVE)) {
            //Log.d("pebble app R", "Got intent to receive");
            //Log.d(TAG,"WAT UUID -" + WatchUUID + "- this");

            final UUID receivedUuid = (UUID) intent.getSerializableExtra(Constants.APP_UUID);
            Log.d(TAG, "APP UUID -" + receivedUuid + "- this");
            // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
            if (!WatchUUID.equals(receivedUuid)) {
                Log.d(TAG , "Notmy uuid");
                return;
            }
            final int transactionId = intent.getIntExtra(TRANSACTION_ID, -1);
            final String jsonData = intent.getStringExtra(MSG_DATA);
            if (jsonData == null || jsonData.isEmpty()) {
                Log.d(TAG, "jsonData null");
                return;
            }
            try {
                final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
                Long zoneValue = data.getUnsignedIntegerAsLong(KEY_ZONE);
                Long cmdValue = data.getUnsignedIntegerAsLong(KEY_CMD);
// do what you need with the data
                if (zoneValue != null && cmdValue != null) {
                    //TODO throw error if teither of these dont exist and tell pebble?

                    //TODO put some more logic around getting zone value to verify its an int 0-9
                    int zone = zoneValue.intValue();
                    int cmd = cmdValue.intValue();
                    Log.d(TAG, "going to turn zone " + zone + " cmd " + cmd);
                    //Switch statement to see cmd (on/off) 0 = off, 1= on, then send the command to the controler with zone
                    switch (cmd) {
                        case 0:
                            //Turning off
                            contCmd.LightsOff(zone);
                            contCmd.appState.setOnOff(zone, false);
                            break;
                        case 1:
                            //Turning on
                            contCmd.LightsOn(zone);
                            contCmd.appState.setOnOff(zone, true);
                            break;
                    }
                    Log.d(TAG, "got data");
                    Log.d(TAG,  " ZONE "+ zone);
                }
                //Todo add exception handle if cant send the message
                //TODO if statement about wifi and phone connectivity can go here, this is what initialises the above that does the dirty work

                PebbleKit.sendAckToPebble(context, transactionId);

            } catch (JSONException e) {
                Log.d(TAG,"failed received -> dict " + e);
                return;
            }
        }
    }
}
