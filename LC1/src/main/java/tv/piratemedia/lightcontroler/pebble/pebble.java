package tv.piratemedia.lightcontroler.pebble;

import android.content.Context;
import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;
import java.util.UUID;
import tv.piratemedia.lightcontroler.controlCommands;
import tv.piratemedia.lightcontroler.controller;

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
public class pebble {
    static final UUID appUuid = UUID.fromString("1d6c7f01-d948-42a6-aa4e-b2084210ebbc");
    private static final int KEY_CMD = 3;
    private static final int KEY_ZONE = 2;

    // Create a new dictionary
    PebbleDictionary dict = new PebbleDictionary();
/*
Pebble related activities. Pass through context, and data receiver, so we can then pause it from Controller

 */

// todo add the ability to send data to the watch.Need to send Zone data to the watch so it can display it

    public static void pebbleaction(Context ctx){
        Log.d("pebble app", "starting onResume in pebble java");
        boolean isConnected = PebbleKit.isWatchConnected(ctx);
        Log.d("Pebble app", "Pebble " + (isConnected ? "is" : "is not") + " connected!");
        //TODO probably should put a if statement to check if pebble is conncted/if we are on the right wifi, so we dont do any work we done need to
        // Create a new receiver to get AppMessages from the C app
        // Create a new controller instance so we can send commands to the wifi controller
        final controller mCont = new controller();
        final controlCommands contCmd;
        contCmd = new controlCommands(ctx, mCont.mHandler);

        PebbleDataReceiver dataReceiver = new PebbleDataReceiver(appUuid) {

            @Override
            public void receiveData(Context context, int transaction_id, PebbleDictionary dict) {
                Log.d("pebble app", "pebble java" + dict + " was received by the android app");
                //declaring variables that are the same as the ones that come from pebble app
                Long zoneValue = dict.getUnsignedIntegerAsLong(KEY_ZONE);
                Long cmdValue = dict.getUnsignedIntegerAsLong(KEY_CMD);
                if (zoneValue != null && cmdValue != null) {
                    //TODO throw error if teither of these dont exist and tell pebble?

                    //TODO put some more logic around getting zone value to verify its an int 0-9
                    int zone = zoneValue.intValue();
                    int cmd = cmdValue.intValue();
                    Log.d("pebble app", "going to turn zone " + zone + " cmd " + cmd);
                    //Switch statement to see cmd (on/off) 0 = off, 1= on, then send the command to the controler with zone
                    switch(cmd){
                        case 0:
                            //Turning off
                            contCmd.LightsOff(zone);
                            contCmd.appState.setOnOff(zone, false);
                            break;
                        case 1:
                            //Turning on
                            contCmd.LightsOn(zone);
                            contCmd.appState.setOnOff(zone,true);
                            break;
                    }
                    /* old switch statement
                    switch (zone) {
                        case 0:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(zone));
                            if (contcmd.appState.getOnOff(zone) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + zone + " was off, turning on");
                                contcmd.LightsOn(zone);
                                contcmd.appState.setOnOff(zone, true);
                            } else {
                                Log.d("pebble app", "Zone " + zone + " was on, turning off");
                                contcmd.LightsOff(zone);
                                contcmd.appState.setOnOff(zone, false);
                            }
                            break;
                        case 1:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(zone));
                            if (contcmd.appState.getOnOff(zone) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 2:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 3:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 4:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 5:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 6:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 7:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 8:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        case 9:
                            Log.d("pebble app", "Zone state is " + contcmd.appState.getOnOff(cmd));
                            if (contcmd.appState.getOnOff(cmd) == false) {
                                //TODO add some feedback to the pebble if something has gone wrong?
                                Log.d("pebble app", "Zone " + cmd + " was off, turning on");
                                contcmd.LightsOn(cmd);
                                contcmd.appState.setOnOff(cmd, true);
                            } else {
                                Log.d("pebble app", "Zone " + cmd + " was on, turning off");
                                contcmd.LightsOff(cmd);
                                contcmd.appState.setOnOff(cmd, false);
                            }
                            break;
                        default:
                            Log.d("pebble app", "something has gone wrong. default switch");
                            break;
                    } */
                }
                //Todo add exception handle if cant send the message
                PebbleKit.sendAckToPebble(context, transaction_id);
            }

        };
        //TODO if statement about wifi and phone connectivity can go here, this is what initialises the above that does the dirty work
        /* sudo code: if (wifi connected, and wifi is the one specified in settings and watch is connected) then */
        PebbleKit.registerReceivedDataHandler(ctx, dataReceiver);

    }
}
