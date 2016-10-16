package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.os.Handler;
import tv.piratemedia.lightcontroler.api.Provider;

/**
 * Created by Eliot Stocker on 16/10/2016.
 */
public class MiiLightProvider {
    private UDPConnection UDPC;
    private Context context;

    public MiiLightProvider(Context c, Handler h) {
        UDPC = new UDPConnection(c, h);
        context = c;
    }

    private void onLightOn(String Zone, String Type) {

    }

    private void onLightOff(String Zone, String Type) {

    }

    public void sendCommand(Provider provider, String action, Context context) {
        sendCommand(provider, action, -1, -1);
    }

    public void sendCommand(Provider provider, String action, int Type, int Zone) {
        //should run command async here
    }
}
