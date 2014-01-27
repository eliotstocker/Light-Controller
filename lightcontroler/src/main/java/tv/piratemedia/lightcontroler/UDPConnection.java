package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPConnection {
    public static String CONTROLLERIP = "";
    public static int CONTROLLERPORT = 0;

    public UDPConnection(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        CONTROLLERIP = prefs.getString("pref_light_controller_ip", "192.168.0.255");
        CONTROLLERPORT = Integer.parseInt(prefs.getString("pref_light_controller_port", "8899"));
    }

    public void sendMessage(byte[] Bytes) throws IOException {
        DatagramSocket s = new DatagramSocket();
        InetAddress controller = InetAddress.getByName(CONTROLLERIP);
        DatagramPacket p = new DatagramPacket(Bytes, 3,controller,CONTROLLERPORT);
        s.send(p);
    }
}
