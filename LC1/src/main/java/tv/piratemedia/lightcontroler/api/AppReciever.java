package tv.piratemedia.lightcontroler.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class AppReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        Set<String> enabled = prefs.getStringSet("enabled_api_apps", new HashSet<String>());

        if(intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getEncodedSchemeSpecificPart();
            Log.d("package", "Package Removed:"+packageName);
            if(enabled.contains(packageName)) {
                enabled.remove(packageName);
                prefs.edit().putStringSet("enabled_api_apps", enabled).apply();
                Log.d("package", "API Permission removed");
            }
        }
    }
}
