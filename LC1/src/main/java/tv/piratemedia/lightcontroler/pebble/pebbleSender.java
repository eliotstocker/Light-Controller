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
    Context context;
    PebbleDictionary dict = new PebbleDictionary();

    private String TAG = "Pebble Sender";

    public pebbleSender(Context context){
        this.context = context;
    }

    /* Send state information of the lights to the watchapp
    *
    * */
    public void sendState(controlCommands contCmd){
        Boolean zone0state = contCmd.appState.getOnOff(0);
        Log.d(TAG, "zone 0 state is " + zone0state );


        //Lets setup to send to the watch
        dict.addString(AppKeyZone0status, zone0state.toString());
        PebbleKit.sendDataToPebble(context, WatchUUID, dict);

    }
    /*Initial connect
    * method that is called from onCreate() in main activity, so when the android app starts, we will see if the watch is connected,
    * start the pebble watch app, then send light state data to the watch. The watch will store this data for later use (WIP)
    * */
    public void initialConnect(controlCommands contCmd){
        Log.d(TAG, "initial connect");
        //Is watch connected?
        Boolean isConnected = PebbleKit.isWatchConnected(context);
        Log.d(TAG, "Is connected? " + isConnected);
        //If watch is connected start app then send state
        if(isConnected) {
            PebbleKit.startAppOnPebble(context, WatchUUID);
            //If state of zone x is on, then add a byte value of 1 to pebbledictionary, if its false, then add 0
            if(contCmd.appState.getOnOff(0)) dict.addInt8(AppKeyZone0status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone0status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(1)) dict.addInt8(AppKeyZone1status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone1status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(2)) dict.addInt8(AppKeyZone2status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone2status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(3)) dict.addInt8(AppKeyZone3status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone3status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(4)) dict.addInt8(AppKeyZone4status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone4status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(5)) dict.addInt8(AppKeyZone5status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone5status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(6)) dict.addInt8(AppKeyZone6status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone6status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(7)) dict.addInt8(AppKeyZone7status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone7status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(8)) dict.addInt8(AppKeyZone8status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone8status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(9)) dict.addInt8(AppKeyZone9status, Byte.valueOf("1"));
            else dict.addInt8(AppKeyZone9status, Byte.valueOf("0"));

            PebbleKit.sendDataToPebble(context, WatchUUID, dict);
        }else Log.d(TAG, "not connected");
    }

}
