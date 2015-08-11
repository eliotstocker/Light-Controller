package tv.piratemedia.lightcontroler.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import tv.piratemedia.lightcontroler.utils;

/**
 * Created by mrwhale on 19/01/15.
 * This class will listen for changes in state of wifi, as to know whether we are connected to the correct SSID so then we can display a card on the watch
 */
public class wifiConnectionListener extends BroadcastReceiver {
    //private Context mCtx;
    private GoogleApiClient mApiClient;

    public void connectToWatch(Context context){
        //setup google API connnection to wearable
        mApiClient = new GoogleApiClient.Builder(context)
                .addApi( Wearable.API )
                .build();
        mApiClient.connect();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        utils cmd = new utils(context);
        final SharedPreferences prefs = context.getSharedPreferences(WearSettings.NETWORKS_PREFS, Context.MODE_PRIVATE);

        Log.d("wear", "connecting to: "+cmd.getWifiName());
        
        if(prefs.getBoolean(cmd.getWifiName(), false)) {
            connectToWatch(context);
            final com.google.android.gms.common.api.PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    final List<Node> nodes = result.getNodes();
                    if (nodes != null) {
                        for (int i = 0; i < nodes.size(); i++) {
                            final Node node = nodes.get(i);
                            Wearable.MessageApi.sendMessage(mApiClient, node.getId(), "/wifi-connected", null);
                        }
                    }
                }
            });
        } else {
            connectToWatch(context);
            final com.google.android.gms.common.api.PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    final List<Node> nodes = result.getNodes();
                    if (nodes != null) {
                        for (int i = 0; i < nodes.size(); i++) {
                            final Node node = nodes.get(i);
                            Wearable.MessageApi.sendMessage(mApiClient, node.getId(), "/wifi-disconnected", null);
                        }
                    }
                }
            });
        }
    }
}
