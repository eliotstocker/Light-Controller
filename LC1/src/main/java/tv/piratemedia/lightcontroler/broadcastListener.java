package tv.piratemedia.lightcontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by mrwhale on 19/01/15.
 * This class will listen for changes in state of wifi, as to know whether we are connected to the correct SSID so then we can display a card on the watch
 */
public class broadcastListener extends BroadcastReceiver {
    //private Context mCtx;
    private GoogleApiClient mApiClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO: Have this in settings so user can set their SSID that has the wifi bridge, or automatically retrieve and save it when issuing successful commands
        // This will eventually put a card notification on the wear watch when the user is wifi range of the SSID that has the wifi bridge, The card will then be
        // used to swipe across to commands list. This is so the commands are even more accessible. Instead of having to start the app when you want to do it. It
        // will always be there when connected.
        Log.d("BroadcastListener", "There was a change in state");

        utils cmd = new utils(context);
        if(cmd.getWifiName().equalsIgnoreCase("ivegotinternet24"))
        {
            Log.d("broadcastListener", "you are connected to SSID");
            mApiClient = new GoogleApiClient.Builder(context)
                    .addApi( Wearable.API )
                    .build();
            mApiClient.connect();
            final com.google.android.gms.common.api.PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                @Override
                public void onResult(NodeApi.GetConnectedNodesResult result) {
                    final List<Node> nodes = result.getNodes();
                    if (nodes != null) {
                        for (int i = 0; i < nodes.size(); i++) {
                            final Node node = nodes.get(i);
                            Log.d("utils","message sent");
                            Wearable.MessageApi.sendMessage(mApiClient, node.getId(), "/Hi there", null);
                        }
                    }
                }
            });


        }
    }
}
