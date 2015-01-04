package tv.piratemedia.lightcontroler;


import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayer";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        // if ("/MESSAGE".equals(messageEvent.getPath())) {
        Log.d(TAG, "message received" + messageEvent.getPath());
        controller mCont = new controller();
        controlCommands cmd;
        cmd = new controlCommands(this, mCont.mHandler);
        switch (messageEvent.getPath()){
            case "/1":
                if (cmd.appState.getOnOff(1) == false) {
                    cmd.LightsOn(1);
                    cmd.appState.setOnOff(1, true);
                    Log.d(TAG, "lights in zone 1 on");
                }
                else if (cmd.appState.getOnOff(1) == true){
                    cmd.LightsOff(1);
                    cmd.appState.setOnOff(1, false);
                    Log.d(TAG, "lights in zone 1 off");
                }
                break;
            case "/2":
                if (cmd.appState.getOnOff(2) == false) {
                    cmd.LightsOn(2);
                    cmd.appState.setOnOff(2, true);
                    Log.d(TAG, "lights in zone 2 on");
                }
                else if (cmd.appState.getOnOff(2) == true){
                    cmd.LightsOff(2);
                    cmd.appState.setOnOff(2, false);
                    Log.d(TAG, "lights in zone 2 off");
                }
                break;
            case "/3":
                if (cmd.appState.getOnOff(3) == false) {
                    cmd.LightsOn(3);
                    cmd.appState.setOnOff(3, true);
                    Log.d(TAG, "lights in zone 3 on");
                }
                else if (cmd.appState.getOnOff(3) == true){
                    cmd.LightsOff(3);
                    cmd.appState.setOnOff(3, false);
                    Log.d(TAG, "lights in zone 3 off");
                }
                break;
            case "/4":
                if (cmd.appState.getOnOff(4) == false) {
                    cmd.LightsOn(4);
                    cmd.appState.setOnOff(4, true);
                    Log.d(TAG, "lights in zone 4 on");
                }
                else if (cmd.appState.getOnOff(4) == true){
                    cmd.LightsOff(4);
                    cmd.appState.setOnOff(4, false);
                    Log.d(TAG, "lights in zone 4 off");
                }
                break;
        }

    //}
    }
}