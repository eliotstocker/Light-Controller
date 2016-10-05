package tv.piratemedia.lightcontroler;

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

/**
 * Created by mrwhale on 19/01/15.
 * This class will listen for changes in state of wifi, as to know whether we are connected to the correct SSID so then we can display a card on the watch
 * I guess we done need this class anymore? has old code in there. (referring statically to my home ssid
 */
public class broadcastListener extends BroadcastReceiver {
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
        //TODO: Have this in settings so user can set their SSID that has the wifi bridge, or automatically retrieve and save it when issuing successful commands

        Log.d("BroadcastListener", "There was a change in state");

        utils cmd = new utils(context);
        //Go\et SSID from prefs to do the check on
        final String ssid = context.getString(R.string.ssid_name);
        Log.d("broadcastlistener","ssid from prefs " + ssid);
        // todo Send up zone names to watch so it can rename them all on the watch. send in array.


        if(cmd.getWifiName().equalsIgnoreCase("ivegotinternet24"))
        {
            Log.d("broadcastListener", "you are connected to SSID");
            connectToWatch(context);
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
