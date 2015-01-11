package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.conn.util.InetAddressUtils;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
/**
 * Created by eliotstocker on 15/11/14.
 */
public class utils extends Activity implements GoogleApiClient.ConnectionCallbacks  {
    public final static int IP_ADDRESS = 0;
    public final static int BROADCAST_ADDRESS = 1;

    private GoogleApiClient mApiClient;

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

            //TODO: Have this in settings so user can set their SSID that has the wifi bridge, or automatically retrieve and save it when issuing successful commands
            // This will eventually put a card notification on the wear watch when the user is wifi range of the SSID that has the wifi bridge, The card will then be
            // used to swipe across to commands list. This is so the commands are even more accessible. Instead of having to start the app when you want to do it. It
            // will always be there when connected.
            // Can probably put this in the notification class or a new one that handles wear notifs. here is good for now because I know it will get executed :P
            if(getWifiName().equalsIgnoreCase("ivegotinternet24"))
            {
                Log.d("utils","you are connected to SSID");
                mApiClient = new GoogleApiClient.Builder(mCtx)
                        .addApi( Wearable.API )
                        .build();
                mApiClient.connect();
                final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mApiClient);
                nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult result) {
                        final List<Node> nodes = result.getNodes();
                        if (nodes != null) {
                            for (int i = 0; i < nodes.size(); i++) {
                                final Node node = nodes.get(i);
                                Wearable.MessageApi.sendMessage(mApiClient, node.getId(), "/Hi there", null);
                            }
                        }
                    }
                });
            }
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
        String SSID = wifi.getConnectionInfo().getSSID();
        return SSID.replace("\"","");
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
