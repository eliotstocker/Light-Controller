package tv.piratemedia.lightcontroler.pebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
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
    PebbleDictionary dict = new PebbleDictionary();


    controller mCont = new controller();
    controlCommands contCmd;

    /* Send state information of the lights to the watchapp
    *
    * */
    public void sendState(Context context, controlCommands contCmd){
        //contCmd = new controlCommands(context, mCont.mHandler);

        Boolean zone0state = contCmd.appState.getOnOff(0);
        Log.d("Pebble sender", "zone 0 state is " + zone0state );


        //Lets setup to send to the watch
        dict.addString(AppKeyZone0status, zone0state.toString());

        PebbleKit.sendDataToPebble(context, WatchUUID, dict);

    }

}
