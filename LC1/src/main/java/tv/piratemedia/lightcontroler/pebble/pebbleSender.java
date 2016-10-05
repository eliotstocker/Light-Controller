package tv.piratemedia.lightcontroler.pebble;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import static tv.piratemedia.lightcontroler.Constants.WatchUUID;
import tv.piratemedia.lightcontroler.controlCommands;
/**
 * Created by mrwhale on 27/07/16.
 * This class serves as a way for the Lightcontroller to send out data to the watch
 * For example, this will be used to send the watch state information of the lights,
 * and setting information (when i get around to implementing this, tell the watch onyl to show x Zones)
 */
public class pebbleSender {
    // Create variables to match the appmessage keys in the watch app
    //Initialise varibales that hold the same key numbers as that in the watch app
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
    final int AppKeyZone1Name = 15;
    final int AppKeyZone2Name = 16;
    final int AppKeyZone3Name = 17;
    final int AppKeyZone4Name = 18;
    final int AppKeyZone5Name = 19;
    final int AppKeyZone6Name = 20;
    final int AppKeyZone7Name = 21;
    final int AppKeyZone8Name = 22;

    Context context;
    PebbleDictionary dictWhite = new PebbleDictionary();
    PebbleDictionary dictColor = new PebbleDictionary();

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
        //dict.addString(AppKeyZone0status, zone0state.toString());
        //PebbleKit.sendDataToPebble(context, WatchUUID, dict);

    }

    /*
    * Class that will handle gathering zone names and then send them up to the watch for storage.
    * Plan is for this class to be only called when you finish updaing the zone names in the settings page
    * Also cgoing to be called if the pebble looks in self storage, and cant see that it has any names stored
    * Pebble storage is persistant, so in theory this only will have to be called once after you install the watch app
    * */
    public void sendZoneNames(){
        Log.d(TAG, "Gathering zone names");
        if(PebbleKit.isWatchConnected(context)){
            //Start  the app on the pebble
            PebbleKit.startAppOnPebble(context, WatchUUID);
            SystemClock.sleep(1000);
            //Create shared preference manager to we can get zone names
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            /*Log.d(TAG, "Zone name 1 = " + preferences.getString("pref_zone1", "MODE_PRIVATE"));
            Log.d(TAG, "Zone name 2 = " + preferences.getString("pref_zone2", "MODE_PRIVATE"));
            Log.d(TAG, "Zone name 3 = " + preferences.getString("pref_zone3", "MODE_PRIVATE"));
            Log.d(TAG, "Zone name 4 = " + preferences.getString("pref_zone4", "MODE_PRIVATE"));

            Log.d(TAG, "Zone name 5 = " + preferences.getString("pref_zone5", "MODE_PRIVATE"));
            Log.d(TAG, "Zone name 6 = " + preferences.getString("pref_zone6", "MODE_PRIVATE"));
            Log.d(TAG, "Zone name 7 = " + preferences.getString("pref_zone7", "MODE_PRIVATE"));
            Log.d(TAG, "Zone name 8 = " + preferences.getString("pref_zone8", "MODE_PRIVATE")); */

            //Get and add zone names to pebble dictionary for sending to pebble. Send zone names in groups based on light type
            // This is to save on incoming message size on the pebble app. try keep things small
            dictColor.addString(AppKeyZone1Name,preferences.getString("pref_zone1", "MODE_PRIVATE"));
            dictColor.addString(AppKeyZone2Name,preferences.getString("pref_zone2", "MODE_PRIVATE"));
            dictColor.addString(AppKeyZone3Name,preferences.getString("pref_zone3", "MODE_PRIVATE"));
            dictColor.addString(AppKeyZone4Name,preferences.getString("pref_zone4", "MODE_PRIVATE"));
            PebbleKit.sendDataToPebble(context, WatchUUID, dictColor);

            dictWhite.addString(AppKeyZone5Name,preferences.getString("pref_zone5", "MODE_PRIVATE"));
            dictWhite.addString(AppKeyZone6Name,preferences.getString("pref_zone6", "MODE_PRIVATE"));
            dictWhite.addString(AppKeyZone7Name,preferences.getString("pref_zone7", "MODE_PRIVATE"));
            dictWhite.addString(AppKeyZone8Name,preferences.getString("pref_zone8", "MODE_PRIVATE"));
            PebbleKit.sendDataToPebble(context, WatchUUID, dictWhite);
        }
    }
    /*Initial connect
    * method that is called from onCreate() in main activity, so when the android app starts, we will see if the watch is connected,
    * start the pebble watch app, then send light state data to the watch. The watch will store this data for later use (WIP)
    * */
    //TODo rename this to something more appropriate - like sendZoneStates or something
    public void initialConnect(controlCommands contCmd){
        Log.d(TAG, "initial connect");
        //If watch is connected start app then send state
        if( PebbleKit.isWatchConnected(context)) {
            PebbleKit.startAppOnPebble(context, WatchUUID);
            //If state of zone x is on, then add a byte value of 1 to pebbledictionary, if its false, then add 0
            // Split the 2 dictionaries into one of easch light type
            if(contCmd.appState.getOnOff(0)) dictColor.addInt8(AppKeyZone0status, Byte.valueOf("1"));
            else dictColor.addInt8(AppKeyZone0status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(1)) dictColor.addInt8(AppKeyZone1status, Byte.valueOf("1"));
            else dictColor.addInt8(AppKeyZone1status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(2)) dictColor.addInt8(AppKeyZone2status, Byte.valueOf("1"));
            else dictColor.addInt8(AppKeyZone2status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(3)) dictColor.addInt8(AppKeyZone3status, Byte.valueOf("1"));
            else dictColor.addInt8(AppKeyZone3status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(4)) dictColor.addInt8(AppKeyZone4status, Byte.valueOf("1"));
            else dictColor.addInt8(AppKeyZone4status, Byte.valueOf("0"));
            //Send colour zones states
            PebbleKit.sendDataToPebble(context, WatchUUID, dictColor);

            if(contCmd.appState.getOnOff(5)) dictWhite.addInt8(AppKeyZone5status, Byte.valueOf("1"));
            else dictWhite.addInt8(AppKeyZone5status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(6)) dictWhite.addInt8(AppKeyZone6status, Byte.valueOf("1"));
            else dictWhite.addInt8(AppKeyZone6status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(7)) dictWhite.addInt8(AppKeyZone7status, Byte.valueOf("1"));
            else dictWhite.addInt8(AppKeyZone7status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(8)) dictWhite.addInt8(AppKeyZone8status, Byte.valueOf("1"));
            else dictWhite.addInt8(AppKeyZone8status, Byte.valueOf("0"));

            if(contCmd.appState.getOnOff(9)) dictWhite.addInt8(AppKeyZone9status, Byte.valueOf("1"));
            else dictWhite.addInt8(AppKeyZone9status, Byte.valueOf("0"));
            //Send white zone states
            PebbleKit.sendDataToPebble(context, WatchUUID, dictWhite);
        }else Log.d(TAG, "not connected");
    }

}
