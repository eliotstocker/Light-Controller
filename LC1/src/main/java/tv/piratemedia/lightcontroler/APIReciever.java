package tv.piratemedia.lightcontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by eliotstocker on 13/01/15.
 */
public class APIReciever extends BroadcastReceiver {
    public static final String LIGHT_ON_INTENT = "tv.piratemedia.lightcontroler.LightOn";
    public static final String LIGHT_OFF_INTENT = "tv.piratemedia.lightcontroler.LightOff";
    public static final String LIGHT_COLOR_INTENT = "tv.piratemedia.lightcontroler.LightColor";

    public static final String TYPE_WHITE = "white";
    public static final String TYPE_COLOR = "color";

    @Override
    public void onReceive(Context context, Intent intent) {
        controlCommands c = new controlCommands(context, null);
        switch(intent.getAction()) {
            case LIGHT_ON_INTENT:
                if(intent.getStringExtra("type").equals(TYPE_WHITE)) {
                    switch(intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOn(0);
                            break;
                        case 1:
                            c.LightsOn(1);
                            break;
                        case 2:
                            c.LightsOn(2);
                            break;
                        case 3:
                            c.LightsOn(3);
                            break;
                        case 4:
                            c.LightsOn(4);
                            break;
                    }
                } else if(intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    switch(intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOn(9);
                            break;
                        case 1:
                            c.LightsOn(5);
                            break;
                        case 2:
                            c.LightsOn(6);
                            break;
                        case 3:
                            c.LightsOn(7);
                            break;
                        case 4:
                            c.LightsOn(8);
                            break;
                    }
                }
                break;
            case LIGHT_OFF_INTENT:
                if(intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    switch(intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOff(0);
                            break;
                        case 1:
                            c.LightsOff(1);
                            break;
                        case 2:
                            c.LightsOff(2);
                            break;
                        case 3:
                            c.LightsOff(3);
                            break;
                        case 4:
                            c.LightsOff(4);
                            break;
                    }
                } else if(intent.getStringExtra("type").equals(TYPE_WHITE)) {
                    switch(intent.getIntExtra("zone", -1)) {
                        case 0:
                            c.LightsOff(9);
                            break;
                        case 1:
                            c.LightsOff(5);
                            break;
                        case 2:
                            c.LightsOff(6);
                            break;
                        case 3:
                            c.LightsOff(7);
                            break;
                        case 4:
                            c.LightsOff(8);
                            break;
                    }
                }
                break;
            case LIGHT_COLOR_INTENT:
                if(intent.getStringExtra("type").equals(TYPE_COLOR)) {
                    int color = intent.getIntExtra("color", -1);
                    if(color != -1) {
                        int zone = intent.getIntExtra("zone", -1);
                        if(zone > -1 && zone < 5) {
                            c.setColor(zone, color);
                        }
                    }
                }
                break;
        }
    }
}
