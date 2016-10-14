package tv.piratemedia.lightcontroler.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.List;

/**
 * Created by jetoapple on 14/10/2016.
 */
public class ControlProviders {
    private static final String PROVIDER_CAT = "com.piratemedia.lightcontroler.provider";
    private static final String PROVIDER_SELECT_ACTION = "tv.piratemedia.lightcontroler.provider.Select";

    public static Provider[] listProviders(Context context) {
        final PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.addCategory(PROVIDER_CAT);
        intent.setAction(PROVIDER_SELECT_ACTION);
        List<ResolveInfo> infos = pm.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER);

        Provider[] list = new Provider[infos.size()];
        int i = 0;
        for (ResolveInfo info : infos) {
            Log.d("provider", info.loadLabel(pm).toString() + ", " + info.activityInfo.packageName + ", " + info.activityInfo.name.substring(info.activityInfo.packageName.length()));
            list[i] = new Provider(info.loadLabel(pm).toString(), info.activityInfo.packageName, info.activityInfo.name.substring(info.activityInfo.packageName.length()));
            //info.activityInfo.metaData.get
            i++;
        }
        return list;
    }

    public static void setProvider(Context context, String pkg) {

    }

    public static void clearProvider(Context context) {

    }

    public static void getCurrentProvider(Context context) {
        final PackageManager pm = context.getPackageManager();

    }
}
