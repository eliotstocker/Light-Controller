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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

public class UDPConnection {
    public static String CONTROLLERIP = "";
    public static int CONTROLLERPORT = 0;
    public static int CONTROLLERADMINPORT = 48899;
    private utils Utils;
    private UDP_Server server = null;
    private SharedPreferences prefs;
    private static Context mCtx;

    private boolean onlineMode = false;

    public UDPConnection(Context context) {
        mCtx = context;
        Utils = new utils(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setOnlineMode(boolean online) {
        onlineMode = online;
    }

    public void sendMessage(byte[] Bytes) throws IOException {
        if(!onlineMode) {
            String NetworkBroadCast = "192.168.0.255";
            try {
                NetworkBroadCast = Utils.getWifiIP(utils.BROADCAST_ADDRESS);
            } catch (ConnectionException e) {
                e.printStackTrace();
                return;
            }
            CONTROLLERIP = prefs.getString("pref_light_controller_ip", NetworkBroadCast);
            CONTROLLERPORT = Integer.parseInt(prefs.getString("pref_light_controller_port", "8899"));
            DatagramSocket s = new DatagramSocket();
            InetAddress controller = InetAddress.getByName(CONTROLLERIP);
            DatagramPacket p = new DatagramPacket(Bytes, 3, controller, CONTROLLERPORT);
            s.send(p);
        } else {
            //send message in online mode;
        }
    }

    public void sendAdminMessage(byte[] Bytes) throws IOException {
        if(server == null) {
            server = new UDP_Server();
            server.runUdpServer();
        }

        String NetworkBroadCast = null;
        try {
            NetworkBroadCast = Utils.getWifiIP(utils.BROADCAST_ADDRESS);
        } catch (ConnectionException e) {
            e.printStackTrace();
            return;
        }
        DatagramSocket s = new DatagramSocket();
        InetAddress controller = InetAddress.getByName(NetworkBroadCast);
        DatagramPacket p = new DatagramPacket(Bytes, Bytes.length, controller, CONTROLLERADMINPORT);
        s.setBroadcast(true);
        s.send(p);
    }

    public void destroyUDPC() {
        Log.d("controller", "destroy");
        if(server != null) {
            server.stop_UDP_Server();
        }
    }

    class UDP_Server {
        private AsyncTask<Void, Void, Void> async;
        private boolean Server_aktiv = true;

        @SuppressLint("NewApi")
        public void runUdpServer() {
            async = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    byte[] lMsg = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
                    DatagramSocket ds = null;

                    try {
                        ds = new DatagramSocket(UDPConnection.CONTROLLERADMINPORT);
                        while (Server_aktiv) {
                            ds.receive(dp);
                            String Data = new String(dp.getData());
                            String[] parts = Data.split(",");
                            if(parts.length > 1) {
                                if(Utils.validIP(parts[0]) && Utils.validMac(parts[1])) {
                                    //this is the discovery response;
                                    Log.d("Packets", "Discovered Host: "+parts[1]);
                                    Message m = new Message();
                                    m.what = controlCommands.DISCOVERED_DEVICE;
                                    m.obj = parts;
                                    ((controller)mCtx).mHandler.sendMessage(m);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (ds != null) {
                            ds.close();
                        }
                    }

                    return null;
                }
            };

            if (Build.VERSION.SDK_INT >= 11)
                async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else async.execute();
        }

        public void stop_UDP_Server() {
            Server_aktiv = false;
        }
    }
}
