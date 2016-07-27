package tv.piratemedia.lightcontroler.pebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.BoringLayout;
import android.util.Log;
import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import static com.getpebble.android.kit.Constants.INTENT_APP_RECEIVE;
import static tv.piratemedia.lightcontroler.Constants.WatchUUID;
import org.json.JSONException;
import static com.getpebble.android.kit.Constants.MSG_DATA;
import static com.getpebble.android.kit.Constants.TRANSACTION_ID;
import tv.piratemedia.lightcontroler.controlCommands;
import tv.piratemedia.lightcontroler.controller;
import tv.piratemedia.lightcontroler.controlPreferences;

import java.util.UUID;
/**
 * Created by harry on 27/07/16.
 * This class serves as a way for the Lightcontroller to send out data to the watch
 * For example, this will be used to send the watch state information of the lights,
 * and setting information (when i get around to implementing this, tell the watch onyl to show x Zones)
 */
public class pebbleSender {
    // Create variables to match the appmessage keys in the watch app
    final int AppKeyZone0status = 5;
    final int AppKeyZone1status = 6;
    final int AppKeyZone2status = 7;
    final int AppKeyZone3status = 8;
    final int AppKeyZone4status = 9;
    final int AppKeyZone5status = 10;
    final int AppKeyZone6status = 11;
    final int AppKeyZone7status = 12;
    final int AppKeyZone8status = 13;
    final int AppKeyZone9status = 14;

    PebbleDictionary dict = new PebbleDictionary();

    private String TAG = "Pebble Sender";

    /* Send state information of the lights to the watchapp
    *
    * */
    public void sendState(Context context, controlCommands contCmd){
        Boolean zone0state = contCmd.appState.getOnOff(0);
        Log.d(TAG, "zone 0 state is " + zone0state );


        //Lets setup to send to the watch
        dict.addString(AppKeyZone0status, zone0state.toString());

        PebbleKit.sendDataToPebble(context, WatchUUID, dict);

    }

    public void initialConnect(Context context, controlCommands contCmd){
        Log.d(TAG, "initial connect");
        //Is watch connected?
        Boolean isConnected = PebbleKit.isWatchConnected(context);
        Log.d(TAG, "Is connected? " + isConnected);

        //If connected, lets gather states and send to watch

        Boolean zone0state = contCmd.appState.getOnOff(0);
        Boolean zone1state = contCmd.appState.getOnOff(1);
        Boolean zone2state = contCmd.appState.getOnOff(2);
        Boolean zone3state = contCmd.appState.getOnOff(3);
        Boolean zone4state = contCmd.appState.getOnOff(4);
        Boolean zone5state = contCmd.appState.getOnOff(5);
        Boolean zone6state = contCmd.appState.getOnOff(6);
        Boolean zone7state = contCmd.appState.getOnOff(7);
        Boolean zone8state = contCmd.appState.getOnOff(8);
        Boolean zone9state = contCmd.appState.getOnOff(9);

        dict.addString(AppKeyZone0status,zone0state.toString());
        dict.addString(AppKeyZone1status,zone1state.toString());
        dict.addString(AppKeyZone2status,zone2state.toString());
        dict.addString(AppKeyZone3status,zone3state.toString());
        dict.addString(AppKeyZone4status,zone4state.toString());
        dict.addString(AppKeyZone5status,zone5state.toString());
        dict.addString(AppKeyZone6status,zone6state.toString());
        dict.addString(AppKeyZone7status,zone7state.toString());
        dict.addString(AppKeyZone8status,zone8state.toString());
        dict.addString(AppKeyZone9status,zone9state.toString());

        PebbleKit.sendDataToPebble(context, WatchUUID, dict);


    }

}
