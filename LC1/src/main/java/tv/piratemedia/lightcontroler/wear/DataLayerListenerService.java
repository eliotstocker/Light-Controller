package tv.piratemedia.lightcontroler.wear;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import tv.piratemedia.lightcontroler.R;
import tv.piratemedia.lightcontroler.controlCommands;
import tv.piratemedia.lightcontroler.controller;

public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = "DataLayer";
    private GoogleApiClient mApiClient;

    public void connectToWatch(Context context){
        //setup google API connnection to wearable
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi( Wearable.API )
                .build();
        mApiClient.connect();

    }
    
    @Override
    //On message received event, does an action when the handheld app receives a message from the watch
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        // if ("/MESSAGE".equals(messageEvent.getPath())) {
        // Create a new controller instance so we can send commands to the wifi controller
        controller mCont = new controller();
        controlCommands cmd;
        cmd = new controlCommands(this, mCont.mHandler);
        //A switch to find out what message was sent from the watch
        if(messageEvent.getPath().equals("/zones")) {
            connectToWatch(getApplicationContext());
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final com.google.android.gms.common.api.PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    final List<Node> nodes = result.getNodes();
                    if (nodes != null) {
                        for (int i = 0; i < nodes.size(); i++) {
                            final Node node = nodes.get(i);
                            Log.d("Wear","Sending Zone List");
                            List<String> zones = new ArrayList<String>(10);
                            zones.add("All Color");
                            zones.add(prefs.getString("pref_zone1", getApplicationContext().getString(R.string.Zone1)));
                            zones.add(prefs.getString("pref_zone2", getApplicationContext().getString(R.string.Zone2)));
                            zones.add(prefs.getString("pref_zone3", getApplicationContext().getString(R.string.Zone3)));
                            zones.add(prefs.getString("pref_zone4", getApplicationContext().getString(R.string.Zone4)));
                            zones.add(prefs.getString("pref_zone5", getApplicationContext().getString(R.string.Zone1)));
                            zones.add(prefs.getString("pref_zone6", getApplicationContext().getString(R.string.Zone2)));
                            zones.add(prefs.getString("pref_zone7", getApplicationContext().getString(R.string.Zone3)));
                            zones.add(prefs.getString("pref_zone8", getApplicationContext().getString(R.string.Zone4)));
                            zones.add("All White");

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            try {
                                ObjectOutputStream oos = new ObjectOutputStream(bos);
                                oos.writeObject(zones);
                                byte[] bytes = bos.toByteArray();

                                Wearable.MessageApi.sendMessage(mApiClient, node.getId(), "/zones", bytes);
                            } catch(IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else {
            Uri path = Uri.parse(messageEvent.getPath());
            int zone = Integer.parseInt(path.getPathSegments().get(0));
            switch(path.getPathSegments().get(1)) {
                case "on":
                    if(zone > 4) {
                        //white
                        cmd.LightsOn(zone);
                        cmd.appState.setOnOff(zone, true);
                    } else {
                        //color
                        cmd.LightsOn(zone);
                        cmd.appState.setOnOff(zone, true);
                    }
                    break;
                case "off":
                    if(zone > 4) {
                        //white
                        cmd.LightsOff(zone);
                        cmd.appState.setOnOff(zone, false);
                    } else {
                        //color
                        cmd.LightsOff(zone);
                        cmd.appState.setOnOff(zone, false);
                    }
                    break;
                case "level":
                    cmd.appState.setOnOff(zone, true);
                    if(zone > 4) {
                        cmd.LightsOn(zone);
                        if(Integer.parseInt(path.getPathSegments().get(2)) == 1) {
                            cmd.setBrightnessUpOne();
                            Log.d("wear", "up one");
                        } else if(Integer.parseInt(path.getPathSegments().get(2)) == -1) {
                            cmd.setBrightnessDownOne();
                            Log.d("wear", "down one");
                        } else {
                            Log.d("wear", "unknown level: "+path.getPathSegments().get(2));
                        }
                    } else {
                        cmd.setBrightness(zone, Integer.parseInt(path.getPathSegments().get(2)));
                        Log.d("wear", "set brightness for color");
                    }
            }
        }
    }
}
