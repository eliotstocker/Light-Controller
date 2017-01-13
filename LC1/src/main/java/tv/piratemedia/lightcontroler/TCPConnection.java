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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class TCPConnection {
    public static String CONTROLLERIP = "";
    public static int CONTROLLERPORT = 0;
    public static int CONTROLLERADMINPORT = 48899;
    private utils Utils;
    private TCP_Server server = null;
    private SharedPreferences prefs;
    private static Context mCtx;
    private static Handler mHandler;
    private String NetworkBroadCast;

    private boolean onlineMode = false;

    public TCPConnection(Context context, Handler handler) {
        mCtx = context;
        mHandler = handler;
        Utils = new utils(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        NetworkBroadCast = "192.168.0.255";
        try {
            NetworkBroadCast = Utils.getWifiIP(utils.BROADCAST_ADDRESS);
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }

    public void setOnlineMode(boolean online) {
        onlineMode = online;
    }

    public void sendMessage(final byte[] Bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!onlineMode) {
                    CONTROLLERIP = prefs.getString("pref_light_controller_ip", NetworkBroadCast);
                    CONTROLLERPORT = Integer.parseInt(prefs.getString("pref_light_controller_port", "8899"));
                    try {
                        InetAddress controller = InetAddress.getByName(CONTROLLERIP);
                        Socket s = new Socket(controller, CONTROLLERPORT);
                        OutputStream out = s.getOutputStream();
                        DataOutputStream dos = new DataOutputStream(out);

                        dos.writeInt(3);
                        dos.write(Bytes, 0, 3);

                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //send message in online mode;
                }
            }
        }).start();
    }

    public void sendAdminMessage(byte[] Bytes) throws IOException {
        sendAdminMessage(Bytes, false);
    }

    public void sendAdminMessage(final byte[] Bytes, final Boolean Device) {
        if(server == null) {
            server = new TCP_Server();
            server.runTcpServer();
        } else if(!server.Server_aktiv) {
            server.runTcpServer();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String NetworkBroadCast = null;
                if (Device) {
                    CONTROLLERIP = prefs.getString("pref_light_controller_ip", "192.168.0.255");
                    NetworkBroadCast = CONTROLLERIP;
                } else {
                    try {
                        NetworkBroadCast = Utils.getWifiIP(utils.BROADCAST_ADDRESS);
                    } catch (ConnectionException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                try {
                    InetAddress controller = InetAddress.getByName(NetworkBroadCast);
                    Socket s = new Socket(controller, CONTROLLERPORT);
                    OutputStream out = s.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);

                    dos.writeInt(Bytes.length);
                    dos.write(Bytes, 0, Bytes.length);

                    s.close();
                } catch(IOException e) {

                }
            }
        }).start();
    }

    public void destroyTCPC() {
        Log.d("controller", "destroy");
        if(server != null) {
            server.stop_TCP_Server();
        }
    }

    class TCP_Server {
        private AsyncTask<Void, Void, Void> async;
        public boolean Server_aktiv = true;

        @SuppressLint("NewApi")
        public void runTcpServer() {
            Server_aktiv = true;
            async = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ServerSocket ts = null;
                    try {
                        ts = new ServerSocket(TCPConnection.CONTROLLERADMINPORT);
                        ts.setSoTimeout(1000);
                        while (Server_aktiv) {
                            try {
                                Socket s = ts.accept();
                                InputStream in = s.getInputStream();
                                DataInputStream dis = new DataInputStream(in);
                                int len = dis.readInt();
                                byte[] data = new byte[len];
                                if (len > 0) {
                                    dis.readFully(data);
                                }
                                String Data = new String(data);
                                if(Data.startsWith("+ok")) {
                                    if(Data.startsWith("+ok=")) {
                                        Message m = new Message();
                                        m.what = controlCommands.LIST_WIFI_NETWORKS;
                                        m.obj = Data;
                                        mHandler.sendMessage(m);
                                        Server_aktiv = false;
                                    } else {
                                        Message m = new Message();
                                        m.what = controlCommands.COMMAND_SUCCESS;
                                        mHandler.sendMessage(m);
                                        Server_aktiv = false;
                                    }
                                } else {
                                    String[] parts = Data.split(",");
                                    if (parts.length > 1) {
                                        if (Utils.validIP(parts[0]) && Utils.validMac(parts[1])) {
                                            Message m = new Message();
                                            m.what = controlCommands.DISCOVERED_DEVICE;
                                            m.obj = parts;
                                            mHandler.sendMessage(m);
                                            Server_aktiv = false;
                                        }
                                    }
                                }
                            } catch(SocketTimeoutException e) {
                                //no problem
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (ts != null) {
                            try {
                                ts.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return null;
                }
            };

            if (Build.VERSION.SDK_INT >= 11)
                async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else async.execute();
        }

        public void stop_TCP_Server() {
            Server_aktiv = false;
        }
    }
}
