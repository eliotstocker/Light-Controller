package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by eliotstocker on 15/11/14.
 */
public class SaveState {
    private Context mCtx;
    private SharedPreferences State = null;

    public SaveState(Context context) {
        mCtx = context;
        State = mCtx.getSharedPreferences("app_state", Context.MODE_PRIVATE);
    }

    public void setOnOff(int Zone, boolean On) {
        State.edit().putBoolean(Zone+"-power", On).apply();
    }

    public boolean getOnOff(int Zone) {
        return State.getBoolean(Zone+"-power", false);
    }

    public void setBrighness(int Zone, float level) {
        State.edit().putFloat(Zone+"-brightness", level).apply();
    }

    public float getBrightness(int Zone) {
        try {
            return State.getFloat(Zone + "-brightness", 1f);
        } catch(Exception e) {
            return (float)State.getInt(Zone + "-brightness", 20) / 20f;
        }
    }

    public void setColor(int Zone, int Color) {
        State.edit().putInt(Zone+"-color", Color).apply();
    }

    public void removeColor(int Zone) {
        State.edit().remove(Zone+"-color").apply();
    }

    public int getColor(int Zone) {
        if(State.contains(Zone+"-color")) {
            return State.getInt(Zone+"-color", 0);
        }
        return 0;
    }
}
