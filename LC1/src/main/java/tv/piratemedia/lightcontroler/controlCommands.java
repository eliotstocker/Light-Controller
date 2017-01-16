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
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import tv.piratemedia.lightcontroler.api.ControlProviders;
import tv.piratemedia.lightcontroler.api.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class controlCommands {
    public static final int DISCOVERED_DEVICE = 111;
    public static final int LIST_WIFI_NETWORKS = 802;
    public static final int COMMAND_SUCCESS = 222;

    private UDPConnection UDPC;
    public int LastOn = -1;
    public boolean sleeping = false;
    private Context mContext;
    private boolean measuring = false;
    private boolean candling = false;
    public final int[] tolerance = new int[1];
    public SaveState appState = null;

    private Provider provider = null;
    private MiiLightProvider mlp = null;

    public controlCommands(Context context, Handler handler) {
        UDPC = new UDPConnection(context, handler);
        mContext = context;
        tolerance[0] = 25000;
        appState = new SaveState(context);

        provider = ControlProviders.getCurrentProvider(context);
        if(provider == null) {
            mlp = new MiiLightProvider(context, handler);
        }
    }

    public void killUDPC() {
        UDPC.destroyUDPC();
    }

    public void discover() {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "discover", mContext);
        } else {
            Log.d("discovery", "Start Discovery");
            try {
                UDPC.sendAdminMessage("AT+Q\r".getBytes());
                Thread.sleep(100);
                UDPC.sendAdminMessage("Link_Wi-Fi".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                //add alert to tell user we cant send command
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void getWifiNetworks() {
        try {
            UDPC.sendAdminMessage("+ok".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+WSCAN\r\n".getBytes(), true);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setWifiNetwork(String SSID, String Security, String Type, String Password) {
        try {
            UDPC.sendAdminMessage("+ok".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage(("AT+WSSSID="+SSID+"\r").getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage(("AT+WSKEY="+Security+","+Type+","+Password+"\r\n").getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+WMODE=STA\r\n".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+Z\r\n".getBytes(), true);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setWifiNetwork(String SSID) {
        try {
            UDPC.sendAdminMessage("+ok".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage(("AT+WSSSID="+SSID+"\r").getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+WMODE=STA\r\n".getBytes(), true);
            Thread.sleep(100);
            UDPC.sendAdminMessage("AT+Z\r\n".getBytes(), true);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void LightsOn(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "LightOn", Type, zone, mContext);
        } else {
            mlp.sendCommand("LightOn", Type, zone);
        }
        appState.setOnOff(zone, true);
    }

    public void globalOn() {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "GlobalOn", mContext);
        } else {
            mlp.sendCommand("GlobalOn");
        }
        appState.setOnOff(0, true);
        appState.setOnOff(9, true);
    }

    public void LightsOff(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "LightOff", Type, zone, mContext);
        } else {
            mlp.sendCommand("LightOff", Type, zone);
        }
        appState.setOnOff(zone, false);
    }

    public void globalOff() {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "GlobalOff", mContext);
        } else {
            mlp.sendCommand("GlobalOff");
        }
        appState.setOnOff(0, false);
        appState.setOnOff(9, false);
    }

    public void setToWhite(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "LightWhite", Type, zone, mContext);
        } else {
            mlp.sendCommand("LightWhite", Type, zone);
        }
        appState.removeColor(zone);
    }

    public void setBrightnessUpOne(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "IncreaseBrightness", Type, zone, mContext);
        } else {
            mlp.sendCommand("IncreaseBrightness", Type, zone);
        }
    }

    public void setBrightnessDownOne(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "DecreaseBrightness", Type, zone, mContext);
        } else {
            mlp.sendCommand("DecreaseBrightness", Type, zone);
        }
    }

    public void setWarmthUpOne(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "IncreaseTemperature", Type, zone, mContext);
        } else {
            mlp.sendCommand("IncreaseTemperature", Type, zone);
        }
    }

    public void setWarmthDownOne(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "DecreaseTemperature", Type, zone, mContext);
        } else {
            mlp.sendCommand("DecreaseTemperature", Type, zone);
        }
    }

    public void setToFull(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "LightFull", Type, zone, mContext);
        } else {
            mlp.sendCommand("LightFull", Type, zone);
        }
    }

    public void setToNight(String Type, int zone) {
        if(provider != null) {
            ControlProviders.sendCommand(provider, "LightNight", Type, zone, mContext);
        } else {
            mlp.sendCommand("LightNight", Type, zone);
        }
    }

    public void setBrightness(String Type, int zone, float brightness) {
        if(brightness > 1f) {
            brightness = 1f;
        }
        if(brightness < 0f) {
            brightness = 0f;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("value", brightness);
        if(!sleeping) {
            if(provider != null) {
                ControlProviders.sendCommand(provider, "Brightness", Type, zone, data, mContext);
            } else {
                mlp.sendCommand("Brightness", Type, zone, data);
            }
            appState.setBrighness(zone, brightness);
            sleeping = true;
            startTimeout();
        }
    }

    public void finishBrightness(String Type, int zone, float brightness) {
        setBrightness(Type, zone, brightness);
    }

    public void startTimeout() {
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    sleep(100);
                    sleeping = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void setColor(String Type, int zone, int color) {
        Map<String, Object> data = new HashMap<>();
        data.put("value", color);
        if(!sleeping) {
            if(provider != null) {
                ControlProviders.sendCommand(provider, "LightColor", Type, zone, data, mContext);
            } else {
                mlp.sendCommand("LightColor", Type, zone, data);
            }
            appState.setColor(zone, color);
            sleeping = true;
            startTimeout();
        }
    }

    public void toggleDiscoMode(int zoneid) {
        LightsOn(ControlProviders.ZONE_TYPE_COLOR, zoneid);
        byte[] messageBA = new byte[3];
        messageBA[0] = 77;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void discoModeFaster() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 68;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void discoModeSlower() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 67;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    private String startCandleColor;
    private String endCandleColor;
    public void startCandleMode(final int zone) {
        candling = true;
        startCandleColor = "fffc00";
        endCandleColor = "ff4e00";

        final int startInt = Integer.parseInt(startCandleColor.substring(2,4),16);
        final int endInt = Integer.parseInt(endCandleColor.substring(2,4),16);

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while(candling) {
                        Random r = new Random();
                        String newColor = "#ff";
                        if(endInt - startInt == 0) {
                            newColor += Integer.toHexString(startInt);
                        } else {
                            if(endInt - startInt < 0) {
                                newColor += Integer.toHexString(r.nextInt(startInt - (endInt - startInt)));
                            } else {
                                newColor += Integer.toHexString(r.nextInt(endInt - startInt) + startInt);
                            }
                        }
                        if(newColor.length() < 5) {
                            newColor+="f";
                        }
                        newColor += "00";

                        try {
                            setColor(ControlProviders.ZONE_TYPE_COLOR, zone, Color.parseColor(newColor));
                        } catch(IllegalArgumentException e) {

                        }
                        int sleedTime = r.nextInt(150) + 50;
                        TimeUnit.MILLISECONDS.sleep(sleedTime);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void stopCandleMode() {
        candling = false;
    }

    private MediaRecorder mr;
    private FileOutputStream fd;
    private int[] strobeColors = new int[4];
    public void startMeasuringVol(final int zone) {
        strobeColors[0] = Color.parseColor("#FF7400");
        strobeColors[1] = Color.parseColor("#FFAA00");
        strobeColors[2] = Color.parseColor("#00FEFE");
        strobeColors[3] = Color.parseColor("#004DFE");
        measuring = true;
        try {
            fd = new FileOutputStream(new File(mContext.getCacheDir().getPath()+"/check"));
            mr = new MediaRecorder();
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mr.setOutputFile(fd.getFD());
            mr.prepare();
            mr.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while(measuring) {
                        if(getInputVolume() > tolerance[0]) {
                            i++;
                            if(i > 3) {
                                i = 0;
                            }
                            setColor(ControlProviders.ZONE_TYPE_COLOR, zone,strobeColors[i]);
                        }
                        TimeUnit.MILLISECONDS.sleep(50);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void stopMeasuringVol() {
        measuring = false;
        try {
            mr.stop();
            mr.reset();
            mr.release();
            fd.flush();
            fd.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private int getInputVolume() {
        try {
            //mr.getMaxAmplitude();
            int amplitude = mr.getMaxAmplitude();
            fd.flush();
            return amplitude;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}