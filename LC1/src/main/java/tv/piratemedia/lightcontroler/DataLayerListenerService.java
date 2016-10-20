package tv.piratemedia.lightcontroler;


import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import tv.piratemedia.lightcontroler.api.ControlProviders;

/*
Created by Harry Sibenaler (mrwhale)
*/

public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayer";

    @Override
    //On message received event, does an action when the handheld app receives a message from the watch
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        // if ("/MESSAGE".equals(messageEvent.getPath())) {
        // Create a new controller instance so we can send commands to the wifi controller
        Log.d(TAG, "message received" + messageEvent.getPath());
        controller mCont = new controller();
        controlCommands cmd;
        cmd = new controlCommands(this, mCont.mHandler);
        //A switch to find out what message was sent from the watch
        switch (messageEvent.getPath()){
            case "/0":
                if (!cmd.appState.getOnOff(5)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_WHITE, 1);
                    cmd.appState.setOnOff(5, true);
                    Log.d(TAG, "lights in w.zone 1 on");
                }
                else if (cmd.appState.getOnOff(5)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_WHITE, 1);
                    cmd.appState.setOnOff(5, false);
                    Log.d(TAG, "lights in w.zone 1 off");
                }
                break;
            case "/1":
                if (!cmd.appState.getOnOff(6)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_WHITE, 2);
                    cmd.appState.setOnOff(6, true);
                    Log.d(TAG, "lights in w.zone 2 on");
                }
                else if (cmd.appState.getOnOff(6)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_WHITE, 2);
                    cmd.appState.setOnOff(6, false);
                    Log.d(TAG, "lights in w.zone 2 off");
                }
                break;
            case "/2":
                if (!cmd.appState.getOnOff(7)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_WHITE, 3);
                    cmd.appState.setOnOff(7, true);
                    Log.d(TAG, "lights in w.zone 3 on");
                }
                else if (cmd.appState.getOnOff(7)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_WHITE, 3);
                    cmd.appState.setOnOff(7, false);
                    Log.d(TAG, "lights in w.zone 3 off");
                }
                break;
            case "/3":
                if (!cmd.appState.getOnOff(8)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_WHITE, 4);
                    cmd.appState.setOnOff(8, true);
                    Log.d(TAG, "lights in w.zone 4 on");
                }
                else if (cmd.appState.getOnOff(8)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_WHITE, 4);
                    cmd.appState.setOnOff(8, false);
                    Log.d(TAG, "lights in w.zone 4 off");
                }
                break;
            case "/4":
                if (!cmd.appState.getOnOff(1)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_COLOR, 1);
                    cmd.appState.setOnOff(1, true);
                    Log.d(TAG, "lights in rgbw zone 1 on");
                }
                else if (cmd.appState.getOnOff(1)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_COLOR, 1);
                    cmd.appState.setOnOff(1, false);
                    Log.d(TAG, "lights in rgbw zone 1 off");
                }
                break;
            case "/5":
                if (!cmd.appState.getOnOff(2)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_COLOR, 2);
                    cmd.appState.setOnOff(2, true);
                    Log.d(TAG, "lights in rgbw zone 2 on");
                }
                else if (cmd.appState.getOnOff(2)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_COLOR, 2);
                    cmd.appState.setOnOff(2, false);
                    Log.d(TAG, "lights in rgbw zone 2 off");
                }
                break;
            case "/6":
                if (!cmd.appState.getOnOff(3)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_COLOR, 3);
                    cmd.appState.setOnOff(3, true);
                    Log.d(TAG, "lights in rgbw zone 3 on");
                }
                else if (cmd.appState.getOnOff(3)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_COLOR, 3);
                    cmd.appState.setOnOff(3, false);
                    Log.d(TAG, "lights in rgbw zone 3 off");
                }
                break;
            case "/7":
                if (!cmd.appState.getOnOff(4)) {
                    cmd.LightsOn(ControlProviders.ZONE_TYPE_COLOR, 4);
                    cmd.appState.setOnOff(4, true);
                    Log.d(TAG, "lights in rgbw zone 4 on");
                }
                else if (cmd.appState.getOnOff(4)){
                    cmd.LightsOff(ControlProviders.ZONE_TYPE_COLOR, 4);
                    cmd.appState.setOnOff(4, false);
                    Log.d(TAG, "lights in rgbw zone 4 off");
                }
                break;
        }

    //}
    }
}
