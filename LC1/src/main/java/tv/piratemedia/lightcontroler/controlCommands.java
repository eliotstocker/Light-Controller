package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
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
                Log.d("EliotTest", "Lights ON Global");
                messageBA[0] = 66;
                break;
            case 1:
                Log.d("EliotTest", "Lights ON Zone 1");
                messageBA[0] = 69;
                break;
            case 2:
                Log.d("EliotTest", "Lights ON Zone 2");
                messageBA[0] = 71;
                break;
            case 3:
                Log.d("EliotTest", "Lights ON Zone 3");
                messageBA[0] = 73;
                break;
            case 4:
                Log.d("EliotTest", "Lights ON Zone 4");
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
                Log.d("EliotTest", "Lights OFF Global");
                messageBA[0] = 65;
                break;
            case 1:
                Log.d("EliotTest", "Lights OFF Zone 1");
                messageBA[0] = 70;
                break;
            case 2:
                Log.d("EliotTest", "Lights OFF Zone 2");
                messageBA[0] = 72;
                break;
            case 3:
                Log.d("EliotTest", "Lights OFF Zone 3");
                messageBA[0] = 74;
                break;
            case 4:
                Log.d("EliotTest", "Lights OFF Zone 4");
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
                Log.d("EliotTest", "Lights White Global");
                messageBA[0] = (byte)194;
                break;
            case 1:
                Log.d("EliotTest", "Lights White Zone 1");
                messageBA[0] = (byte)197;
                break;
            case 2:
                Log.d("EliotTest", "Lights White Zone 2");
                messageBA[0] = (byte)199;
                break;
            case 3:
                Log.d("EliotTest", "Lights White Zone 3");
                messageBA[0] = (byte)201;
                break;
            case 4:
                Log.d("EliotTest", "Lights White Zone 4");
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
            Log.d("EliotTest", "brightness: " + brightness);
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
            Log.d("EliotTest","deg: "+deg+"color: "+dec);
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
        Log.d("EliotTest", "Disco Toggle");
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
        Log.d("EliotTest", "Disco Faster");
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
        Log.d("EliotTest", "Disco Slower");
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

    private int startCandleColor;
    private int endCandleColor;
    public void startCandleMode(final int zone) {
        candling = true;
        startCandleColor = Color.parseColor("#fffc00");
        endCandleColor = Color.parseColor("#ff4e00");
        Log.d("Eliot", "Start: "+startCandleColor+" End: "+endCandleColor);
        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    int i = 0;
                    while(candling) {
                        Random r = new Random();
                        int color = r.nextInt(-endCandleColor - -startCandleColor) + -startCandleColor;
                        setColor(zone,-color);
                        TimeUnit.MILLISECONDS.sleep(50);
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
        Log.d("EliotTest","Start Mic Input in zone: "+zone);
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
                            Log.d("Eliot","Beat");
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
        Log.d("EliotTest","stop Mic Input");
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
