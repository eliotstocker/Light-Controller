
//http://www.technotalkative.com/android-wear-part-5-wearablelistview/
package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
/**
        * Created by mrwhale
        * This class is the main activity class for the wear device, and will, onclick, send message to handheld to turn lights on/off
        * This had to be in its own class as it needs to have the same package name as the mainactivity on the handheld to work properly
        */
public class MainActivity extends Activity implements WearableListView.ClickListener, WearableListView.OnCenterProximityListener{

    private WearableListView mListView;
    private GoogleApiClient mGoogleApiClient;
    private ImageView mCircle;
    private TextView mName;

    //private final float mFadedTextAlpha =
    private final float mFadedTextAlpha = R.integer.action_text_faded_alpha / 100f;
    private final int mFadedCircleColor = R.color.wl_gray;
    private final int mChosenCircleColor = R.color.wl_blue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.listView1);
                mListView.setAdapter(new MyAdapter(MainActivity.this));
                mListView.setClickListener(MainActivity.this);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    private static ArrayList<String> listItems;
    static {
        listItems = new ArrayList<String>();
        listItems.add("White Zone 1");
        listItems.add("White Zone 2");
        listItems.add("White Zone 3");
        listItems.add("White Zone 4");
        listItems.add("RGBW Zone 1");
        listItems.add("RGBW Zone 2");
        listItems.add("RGBW Zone 3");
        listItems.add("RGBW Zone 4");
    }

    @Override
    public void onClick(final WearableListView.ViewHolder viewHolder) {
        Log.d("wear","I pressed " + viewHolder.getPosition());

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
                        switch(viewHolder.getPosition()) {
                            case 0:
                                Log.d("wear","pos 0 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/0", null);
                                break;
                            case 1:
                                Log.d("wear","pos 1 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/1", null);
                                break;
                            case 2:
                                Log.d("wear", "pos 2 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/2", null);
                                break;
                            case 3:
                                Log.d("wear", "pos 3 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/3", null);
                                break;
                            case 4:
                                Log.d("wear", "pos 4 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/4", null);
                                break;
                            case 5:
                                Log.d("wear", "pos 5 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/5", null);
                                break;
                            case 6:
                                Log.d("wear", "pos 6 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/6", null);
                                break;
                            case 7:
                                Log.d("wear", "pos 7 pressed");
                                Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/7", null);
                                break;
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    /*@Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.textView);
    }
*/
    @Override
    public void onCenterPosition(boolean animate) {
        mName.setAlpha(1f);
        ((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        ((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
        mName.setAlpha(mFadedTextAlpha);
    }

    private class MyAdapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;

        private MyAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.row_simple_item_layout, null));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            TextView view = (TextView) holder.itemView.findViewById(R.id.textView);
            view.setText(listItems.get(position).toString());
            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }
    }
}
                           /* package tv.piratemedia.lightcontroler;


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
                                    } */

