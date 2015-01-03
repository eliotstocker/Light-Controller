package tv.piratemedia.lightcontroler;

import android.content.Context;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
/**
 * Created by harry on 3/01/15.
 */
public class DataLayerListenerService extends WearableListenerService {
    private Context mContext;
    private controller.MyHandler mHandler;
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if("/MESSAGE".equals(messageEvent.getPath())) {
            // launch some Activity or do anything you like
            controlCommands cmd;
            cmd = new controlCommands(mContext, mHandler);

            cmd.LightsOff(2);
        }
    }
}
