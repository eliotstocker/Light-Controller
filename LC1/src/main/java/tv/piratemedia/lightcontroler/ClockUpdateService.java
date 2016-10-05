package tv.piratemedia.lightcontroler;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by eliotstocker on 24/06/16.
 */
public class ClockUpdateService extends Service {
    private static final String ACTION_UPDATE =
            "tv.piratemedia.lightcontroler.clock.action.UPDATE";
    private SharedPreferences prefs;

    private final static IntentFilter intentFilter;

    static {
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(ACTION_UPDATE);
    }

    /**
     * BroadcastReceiver receiving the updates.
     */
    private final BroadcastReceiver clockChangedReceiver = new
            BroadcastReceiver() {
                /**
                 * {@inheritDoc}
                 */
                public void onReceive(Context context, Intent intent) {
                    Log.d("wear", "Update Clock");
                    Intent updateIntent = new Intent();
                    updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    updateIntent.putExtra("action", "update");
                    context.sendBroadcast(updateIntent);
                }
            };

    /**
     * {@inheritDoc}
     */
    public IBinder onBind(Intent intent) {
        return null;
    }
    /**
     * {@inheritDoc}
     */
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        registerReceiver(clockChangedReceiver, intentFilter);
    }
    /**
     * {@inheritDoc}
     */
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(clockChangedReceiver);
    }
    /**
     * {@inheritDoc}
     */
    public void onStart(Intent intent, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_UPDATE)) {
                Intent updateIntent = new Intent();
                updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                this.sendBroadcast(updateIntent);
            }
        }
    }
}
