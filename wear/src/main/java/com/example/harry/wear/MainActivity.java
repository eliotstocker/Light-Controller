package com.example.harry.wear;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

    private TextView mTextView;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wearable_button_activity);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onButtonClicked(View target) {
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

                        // or you may want to also check check for a result:
                        final PendingResult<MessageApi.SendMessageResult> pendingSendMessageResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/MESSAGE", null);
                        pendingSendMessageResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                              public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                  if (sendMessageResult.getStatus().getStatusCode()== WearableStatusCodes.SUCCESS) {
                                      Log.d("wear", "SUCCESFULLY SENT");
                                  }
                                  else{
                                       Log.d("wear", "Not succesfully sent");
                                  }
                              }
                         });
                    }
                }
            }
        });
    }
}
