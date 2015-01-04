package tv.piratemedia.lightcontroler;


import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableStatusCodes;

import java.util.List;

public class MainActivity extends Activity {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onConnected(Bundle connectionHint) {
        Log.d("wearablemain", "onConnected(): Successfully connected to Google API client");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (null != mGoogleApiClient && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public void onButtonClicked(final View target) {
        if (mGoogleApiClient == null)
            return;

        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.size(); i++) {
                        final Node node = nodes.get(i);

                        // You can just send a message
                        //Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/MESSAGE", null);
                        switch(target.getId()) {
                            case R.id.button1:
                                Log.d("wear","button 1 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/1", null);
                                break;
                            case R.id.button2:
                                Log.d("wear","button 2 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/2", null);
                                break;
                            case R.id.button3:
                                Log.d("wear", "button 3 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/3", null);
                                break;
                            case R.id.button4:
                                Log.d("wear", "button4 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/4", null);
                                break;
                        }
                        // or you may want to also check check for a result:
                       /* final PendingResult<MessageApi.SendMessageResult> pendingSendMessageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/MESSAGE", null);
                        pendingSendMessageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                              public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                  if (sendMessageResult.getStatus().getStatusCode()== WearableStatusCodes.SUCCESS) {
                                      Log.d("wear", "SUCCESFULLY SENT");


                                  }
                                  else{
                                       Log.d("wear", "Not succesfully sent");
                                  }
                              }
                         });*/
                    }
                }
            }
        });
    }
}
