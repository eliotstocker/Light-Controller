package tv.piratemedia.lightcontroler.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.util.Log;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by Eliot Stocker on 14/10/2016.
 */
public class ControlProviders {
    private static final String PROVIDER_CAT = "com.piratemedia.lightcontroler.provider";
    private static final String PROVIDER_SELECT_ACTION = "tv.piratemedia.lightcontroler.provider.Select";

    public static Provider[] listProviders(Context context) {
        final PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.addCategory(PROVIDER_CAT);
        intent.setAction(PROVIDER_SELECT_ACTION);
        List<ResolveInfo> infos = pm.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER|PackageManager.GET_META_DATA);

        Provider[] list = new Provider[infos.size()];
        int i = 0;
        for (ResolveInfo info : infos) {
            list[i] = new Provider(info.loadLabel(pm).toString(), info.activityInfo.packageName, info.activityInfo.name.substring(info.activityInfo.packageName.length()));
            int XMLResource = info.activityInfo.metaData.getInt(PROVIDER_CAT);
            try {
                XmlResourceParser xpp = pm.getResourcesForApplication(info.activityInfo.applicationInfo).getXml(XMLResource);
                xpp.next();
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if(eventType == XmlPullParser.START_TAG && xpp.getName().equals("control-provider")) {
                        list[i].ColorBrightnessStatefull  = xpp.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "ColorBrightnessStatefull", false);
                        list[i].WhiteBrightnessStatefull  = xpp.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "WhiteBrightnessStatefull", false);
                        list[i].ColorHasTemperature       = xpp.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "ColorHasTemperature", false);
                        list[i].ColorTemperatureStatefull = xpp.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "ColorTemperatureStatefull", false);
                        list[i].WhiteTemperatureStatefull = xpp.getAttributeBooleanValue("http://schemas.android.com/apk/res-auto", "WhiteTemperatureStatefull", false);
                    }
                    eventType = xpp.next();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
