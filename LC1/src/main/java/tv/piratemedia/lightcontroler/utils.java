package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import com.google.android.gms.common.api.GoogleApiClient;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/**
 * Created by eliotstocker on 15/11/14.
 */
public class utils extends Activity implements GoogleApiClient.ConnectionCallbacks  {
    public final static int IP_ADDRESS = 0;
    public final static int BROADCAST_ADDRESS = 1;

    private Context mCtx;

    public utils(Context ctx) {
        mCtx = ctx;
    }

    public boolean isWifiConnection() {
        ConnectivityManager connManager = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    public String getWifiIP(int type) throws ConnectionException{
        if(isWifiConnection()) {
            WifiManager wifi = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow
            switch(type) {
                case BROADCAST_ADDRESS :
                    Log.d("Utils", "get Broadcast");
                    System.setProperty("java.net.preferIPv4Stack", "true");
                    try {
                        for(Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();) {
                            NetworkInterface ni = niEnum.nextElement();
                            if (!ni.isLoopback()) {
                                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses()) {
                                    if(interfaceAddress.getBroadcast() != null) {
                                        return interfaceAddress.getBroadcast().toString().substring(1);
                                    }
                                }
                            }
                        }
                    }catch (SocketException e) {
                        throw new ConnectionException("Cant get Address", ConnectionException.CANT_GET_ADDRESS);
                    }
                    return null;
                case IP_ADDRESS :
                    byte[] bytes = BigInteger.valueOf(dhcp.ipAddress).toByteArray();
                    try {
                        InetAddress address = InetAddress.getByAddress(bytes);
                        return address.getHostAddress();
                    } catch (UnknownHostException e) {
                        throw new ConnectionException("Cant get Address", ConnectionException.CANT_GET_ADDRESS);
                    }
            }
        } else {
            throw new ConnectionException("Wifi not connected", ConnectionException.WIFI_NOT_CONNECTED);
        }
        return null;
    }

    public String getWifiName() {
        WifiManager wifi = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
        return wifi.getConnectionInfo().getSSID().substring(1, wifi.getConnectionInfo().getSSID().length() - 1);
    }

    public String GetWifiMac() {
        WifiManager wifi = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
        return wifi.getConnectionInfo().getBSSID();
    }

    public boolean validIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    public static boolean validHost(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }


    public boolean validMac(String MAC) {
        if (MAC == null || MAC.isEmpty()) return false;
        MAC = MAC.trim();
        if (MAC.length() != 12) return false;

        try {
            Pattern pattern = Pattern.compile("^([0-9A-F]{2}){6}$");
            Matcher matcher = pattern.matcher(MAC);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    public void onConnected(Bundle bundle) {

    }


    @Override
    public void onConnectionSuspended(int i) {

    }
}
