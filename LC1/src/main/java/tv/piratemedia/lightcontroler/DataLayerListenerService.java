package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import tv.piratemedia.lightcontroler.controlCommands;
import tv.piratemedia.lightcontroler.controller;

/**
 * Created by harry on 3/01/15.
 */
/*public class DataLayerListenerService extends WearableListenerService {
    private Context mContext;
    //private tv.piratemedia.lightcontroler.controller.MyHandler mHandler;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if ("/MESSAGE".equals(messageEvent.getPath())) {
            // launch some Activity or do anything you like
            Log.d("datalistener", "Cmd received inside if");
            /*controlCommands cmd;
            cmd = new controlCommands(mContext, mHandler);

            cmd.LightsOff(2);
     //   }
        //Log.d("datalistener", "Cmd received outside if");
   // }
} */

public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "DataLayerSample";
    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if ("/MESSAGE".equals(messageEvent.getPath())) {
            Log.d("datalistener", "Cmd received inside if");
            controller mCont = new controller();
            controlCommands cmd;
            cmd = new controlCommands(this, mCont.mHandler);
            cmd.LightsOff(2);
            Log.d("datalistener","lights in zone 2 off");
        }

    }
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }
    }
}