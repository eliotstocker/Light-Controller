package tv.piratemedia.lightcontroler;


import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayer";
    controller mCont = new controller();
    controlCommands cmd = new controlCommands(this, mCont.mHandler);
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        // if ("/MESSAGE".equals(messageEvent.getPath())) {
        Log.d(TAG, "message received" + messageEvent.getPath());
        //controller mCont = new controller();
        //controlCommands cmd;
        cmd = new controlCommands(this, mCont.mHandler);
        switch (messageEvent.getPath()){
            case "/0":
                if (!cmd.appState.getOnOff(5)) {
                    cmd.LightsOn(5);
                    cmd.appState.setOnOff(5, true);
                    Log.d(TAG, "lights in w.zone 1 on");
                }
                else if (cmd.appState.getOnOff(5)){
                    cmd.LightsOff(5);
                    cmd.appState.setOnOff(5, false);
                    Log.d(TAG, "lights in w.zone 1 off");
                }
                break;
            case "/1":
                if (!cmd.appState.getOnOff(6)) {
                    cmd.LightsOn(6);
                    cmd.appState.setOnOff(6, true);
                    Log.d(TAG, "lights in w.zone 2 on");
                }
                else if (cmd.appState.getOnOff(6)){
                    cmd.LightsOff(6);
                    cmd.appState.setOnOff(6, false);
                    Log.d(TAG, "lights in w.zone 2 off");
                }
                break;
            case "/2":
                if (!cmd.appState.getOnOff(7)) {
                    cmd.LightsOn(7);
                    cmd.appState.setOnOff(7, true);
                    Log.d(TAG, "lights in w.zone 3 on");
                }
                else if (cmd.appState.getOnOff(7)){
                    cmd.LightsOff(7);
                    cmd.appState.setOnOff(7, false);
                    Log.d(TAG, "lights in w.zone 3 off");
                }
                break;
            case "/3":
                if (!cmd.appState.getOnOff(8)) {
                    cmd.LightsOn(8);
                    cmd.appState.setOnOff(8, true);
                    Log.d(TAG, "lights in w.zone 4 on");
                }
                else if (cmd.appState.getOnOff(8)){
                    cmd.LightsOff(8);
                    cmd.appState.setOnOff(8, false);
                    Log.d(TAG, "lights in w.zone 4 off");
                }
                break;
            case "/4":
                if (!cmd.appState.getOnOff(1)) {
                    cmd.LightsOn(1);
                    cmd.appState.setOnOff(1, true);
                    Log.d(TAG, "lights in rgbw zone 1 on");
                }
                else if (cmd.appState.getOnOff(1)){
                    cmd.LightsOff(1);
                    cmd.appState.setOnOff(1, false);
                    Log.d(TAG, "lights in rgbw zone 1 off");
                }
                break;
            case "/5":
                if (!cmd.appState.getOnOff(2)) {
                    cmd.LightsOn(2);
                    cmd.appState.setOnOff(2, true);
                    Log.d(TAG, "lights in rgbw zone 2 on");
                }
                else if (cmd.appState.getOnOff(2)){
                    cmd.LightsOff(2);
                    cmd.appState.setOnOff(2, false);
                    Log.d(TAG, "lights in rgbw zone 2 off");
                }
                break;
            case "/6":
                if (!cmd.appState.getOnOff(3)) {
                    cmd.LightsOn(3);
                    cmd.appState.setOnOff(3, true);
                    Log.d(TAG, "lights in rgbw zone 3 on");
                }
                else if (cmd.appState.getOnOff(3)){
                    cmd.LightsOff(3);
                    cmd.appState.setOnOff(3, false);
                    Log.d(TAG, "lights in rgbw zone 3 off");
                }
                break;
            case "/7":
                if (!cmd.appState.getOnOff(4)) {
                    cmd.LightsOn(4);
                    cmd.appState.setOnOff(4, true);
                    Log.d(TAG, "lights in rgbw zone 4 on");
                }
                else if (cmd.appState.getOnOff(4)){
                    cmd.LightsOff(4);
                    cmd.appState.setOnOff(4, false);
                    Log.d(TAG, "lights in rgbw zone 4 off");
                }
                break;
        }

    //}
    }
}
