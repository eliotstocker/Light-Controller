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
package com.dudeofawesome.limitlessLED;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class controlCommands {
    private UDPConnection UDPC;
    public int LastOn = -1;
    public boolean sleeping = false;
    private Context mContext;
    private boolean measuring = false;
    private boolean candling = false;
    public final int[] tolerance = new int[1];

    public controlCommands(Context context) {
        UDPC = new UDPConnection(context);
        mContext = context;
        tolerance[0] = 25000;
    }

    public void recreateUDPC() {
        UDPC = new UDPConnection(mContext);
    }

    public void LightsOn(int zone) {
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                messageBA[0] = 66;
                break;
            case 1:
                messageBA[0] = 69;
                break;
            case 2:
                messageBA[0] = 71;
                break;
            case 3:
                messageBA[0] = 73;
                break;
            case 4:
                messageBA[0] = 75;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        LastOn = zone;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void LightsOff(int zone) {
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                messageBA[0] = 65;
                break;
            case 1:
                messageBA[0] = 70;
                break;
            case 2:
                messageBA[0] = 72;
                break;
            case 3:
                messageBA[0] = 74;
                break;
            case 4:
                messageBA[0] = 76;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setToWhite(int zone) {
        byte[] messageBA = new byte[3];
        switch(zone) {
            case 0:
                messageBA[0] = (byte)194;
                break;
            case 1:
                messageBA[0] = (byte)197;
                break;
            case 2:
                messageBA[0] = (byte)199;
                break;
            case 3:
                messageBA[0] = (byte)201;
                break;
            case 4:
                messageBA[0] = (byte)203;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void setBrightness(int zoneid, int brightness) {
        if(!sleeping) {
            LightsOn(zoneid);
            byte[] messageBA = new byte[3];
            messageBA[0] = 78;
            messageBA[1] = (byte)(brightness);
            messageBA[2] = 85;
            try {
                UDPC.sendMessage(messageBA);
            } catch (IOException e) {
                e.printStackTrace();
                //add alert to tell user we cant send command
            }
            sleeping = true;
            startTimeout();
        }
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

    public void setColor(int zoneid, int color) {
        if(!sleeping) {
            float[] colors = new float[3];
            Color.colorToHSV(color, colors);
            Float deg = (float) Math.toRadians(-colors[0]);
            Float dec = (deg/((float)Math.PI*2f))*255f;
            if(LastOn != zoneid) {
                LightsOn(zoneid);
            }
            //rotation compensation
            dec = dec + 175;
            if(dec > 255) {
                dec = dec - 255;
            }

            byte[] messageBA = new byte[3];
            messageBA[0] = 64;
            messageBA[1] = (byte)dec.intValue();
            messageBA[2] = 85;
            try {
                UDPC.sendMessage(messageBA);
            } catch (IOException e) {
                e.printStackTrace();
                //add alert to tell user we cant send command
            }
            sleeping = true;
            startTimeout();
        }
    }

    public void toggleDiscoMode(int zoneid) {
        LightsOn(zoneid);
        byte[] messageBA = new byte[3];
        messageBA[0] = 77;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void discoModeFaster() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 68;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
    }

    public void discoModeSlower() {
        byte[] messageBA = new byte[3];
        messageBA[0] = 67;
        messageBA[1] = 0;
        messageBA[2] = 85;
        try {
            UDPC.sendMessage(messageBA);
        } catch (IOException e) {
            e.printStackTrace();
            //add alert to tell user we cant send command
        }
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
                            setColor(zone, Color.parseColor(newColor));
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
        strobeColors[0] = Color.parseColor("#FF0000");
        strobeColors[1] = Color.parseColor("#0000FF");
        strobeColors[2] = Color.parseColor("#FFFF00");
        strobeColors[3] = Color.parseColor("#00FF00");
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
                            setColor(zone,strobeColors[i]);
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