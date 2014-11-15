package tv.piratemedia.lightcontroler;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.preference.PreferenceManager;

/**
 * Created by eliotstocker on 11/11/14.
 */
public class LCBackupAgent extends BackupAgentHelper {
    static final String PREFS_APP_SETTINGS = "settings";
    static final String PREFS_ALARM_SETTINGS = "alarms";

    @Override
    public void onCreate() {
        //backup default preferences
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, "preferences");
        addHelper(PREFS_APP_SETTINGS, helper);
    }
}
