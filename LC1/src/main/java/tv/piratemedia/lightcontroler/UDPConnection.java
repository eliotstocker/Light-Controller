/*
*    Light Controller, to Control wifi LED Lighting
*    Copyright (C) 2014  Eliot Stocker
*
*    This program is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
