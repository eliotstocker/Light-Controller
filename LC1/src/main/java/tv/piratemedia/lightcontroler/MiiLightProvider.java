package tv.piratemedia.lightcontroler;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import tv.piratemedia.lightcontroler.api.ControlProviders;
import tv.piratemedia.lightcontroler.api.Provider;

/**
 * Created by Eliot Stocker on 16/10/2016.
 */
public class MiiLightProvider {
    private UDPConnection UDPC;
    private Context context;
    private int lastOn = -1;

    public MiiLightProvider(Context c, Handler h) {
        UDPC = new UDPConnection(c, h);
        context = c;
    }

    private void onLightOn(int Zone, String Type) {
        byte[] messageBA = new byte[3];
        if(Type.equals(ControlProviders.ZONE_TYPE_COLOR)) {
            switch (Zone) {
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
        } else if(Type.equals(ControlProviders.ZONE_TYPE_WHITE)) {
            switch(Zone) {
                case 0:
                    messageBA[0] = 53;
                    break;
                case 1:
                    messageBA[0] = 56;
                    break;
                case 2:
                    messageBA[0] = 61;
                    break;
                case 3:
                    messageBA[0] = 55;
                    break;
                case 4:
                    messageBA[0] = 50;
                    break;
            }
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        lastOn = Zone;
        UDPC.sendMessage(messageBA);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onLightOff(int Zone, String Type) {
        byte[] messageBA = new byte[3];
        if(Type.equals(ControlProviders.ZONE_TYPE_COLOR)) {
            switch (Zone) {
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
        } else if(Type.equals(ControlProviders.ZONE_TYPE_WHITE)) {
            switch (Zone) {
                case 0:
                    messageBA[0] = 57;
                    break;
                case 1:
                    messageBA[0] = 59;
                    break;
                case 2:
                    messageBA[0] = 51;
                    break;
                case 3:
                    messageBA[0] = 58;
                    break;
                case 4:
                    messageBA[0] = 54;
                    break;
            }
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onGlobalOn() {
        this.onLightOn(0, ControlProviders.ZONE_TYPE_COLOR);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.onLightOn(0, ControlProviders.ZONE_TYPE_WHITE);
    }

    public void onGlobalOff() {
        this.onLightOff(0, ControlProviders.ZONE_TYPE_COLOR);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.onLightOff(0, ControlProviders.ZONE_TYPE_WHITE);
    }

    private int[] values = {2,3,4,5,8,9,10,11,13,14,15,16,17,18,19,20,21,23,24,25};
    public void onSetBrightness(String Type, int Zone, float Brightness) {
        int lz = (Type.equals(ControlProviders.ZONE_TYPE_WHITE) ? 5 : 0) + Zone;
        if(lastOn != lz) {
            this.onLightOn(Zone, Type);
            lastOn = lz;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int val = Math.round(((float)(values.length - 1) * Brightness));

        byte[] messageBA = new byte[3];
        messageBA[0] = 78;
        messageBA[1] = (byte)(values[val]);
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }


    public void onIncreaseBrightness(String Type, int Zone) {
        int lz = (Type.equals(ControlProviders.ZONE_TYPE_WHITE) ? 5 : 0) + Zone;
        if(lastOn != lz) {
            this.onLightOn(Zone, Type);
            lastOn = lz;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] messageBA = new byte[3];
        messageBA[0] = 60;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onDecreaseBrightness(String Type, int Zone) {
        int lz = (Type.equals(ControlProviders.ZONE_TYPE_WHITE) ? 5 : 0) + Zone;
        if(lastOn != lz) {
            this.onLightOn(Zone, Type);
            lastOn = lz;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] messageBA = new byte[3];
        messageBA[0] = 52;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onSetColor(int Zone, int color) {
        if(lastOn != Zone) {
            this.onLightOn(Zone, ControlProviders.ZONE_TYPE_COLOR);
            lastOn = Zone;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        float[] colors = new float[3];
        Color.colorToHSV(color, colors);
        Float deg = (float) Math.toRadians(-colors[0]);
        Float dec = (deg/((float)Math.PI*2f))*255f;
        //rotation compensation
        dec = dec + 175;
        if(dec > 255) {
            dec = dec - 255;
        }

        byte[] messageBA = new byte[3];
        messageBA[0] = 64;
        messageBA[1] = (byte)dec.intValue();
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onSetTemperature(String Type, int Zone, float Temp) {
        //no needed here
    }

    public void onIncreaseTemperature(String Type, int Zone) {
        int lz = (Type.equals(ControlProviders.ZONE_TYPE_WHITE) ? 5 : 0) + Zone;
        if(lastOn != lz) {
            this.onLightOn(Zone, Type);
            lastOn = lz;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] messageBA = new byte[3];
        messageBA[0] = 62;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }


    public void onDecreaseTemperature(String Type, int Zone) {
        int lz = (Type.equals(ControlProviders.ZONE_TYPE_WHITE) ? 5 : 0) + Zone;
        if(lastOn != lz) {
            this.onLightOn(Zone, Type);
            lastOn = lz;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        byte[] messageBA = new byte[3];
        messageBA[0] = 63;
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onSetNight(String Type, int Zone) {
        this.onLightOn(Zone, Type);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] messageBA = new byte[3];
        if(Type.equals(ControlProviders.ZONE_TYPE_WHITE)) {
            switch (Zone) {
                case 0:
                    messageBA[0] = (byte) 185;
                    break;
                case 1:
                    messageBA[0] = (byte) 187;
                    break;
                case 2:
                    messageBA[0] = (byte) 179;
                    break;
                case 3:
                    messageBA[0] = (byte) 186;
                    break;
                case 4:
                    messageBA[0] = (byte) 182;
                    break;
            }
        } else {
            switch (Zone) {
                case 0:
                    messageBA[0] = (byte) 193;
                    break;
                case 1:
                    messageBA[0] = (byte) 198;
                    break;
                case 2:
                    messageBA[0] = (byte) 200;
                    break;
                case 3:
                    messageBA[0] = (byte) 202;
                    break;
                case 4:
                    messageBA[0] = (byte) 204;
                    break;
            }
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onSetFull(String Type, int Zone) {
        this.onLightOn(Zone, Type);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        byte[] messageBA = new byte[3];
        switch(Zone) {
            case 0:
                messageBA[0] = (byte)181;
                break;
            case 1:
                messageBA[0] = (byte)184;
                break;
            case 2:
                messageBA[0] = (byte)189;
                break;
            case 3:
                messageBA[0] = (byte)183;
                break;
            case 4:
                messageBA[0] = (byte)178;
                break;
        }
        messageBA[1] = 0;
        messageBA[2] = 85;
        UDPC.sendMessage(messageBA);
    }

    public void onSetWhite(int Zone) {
        byte[] messageBA = new byte[3];
        switch(Zone) {
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
        UDPC.sendMessage(messageBA);
    }

    public void sendCommand(String action) {
        sendCommand(action, null, -1, null);
    }

    public void sendCommand(String action, String Type, int Zone) {
        sendCommand(action, Type, Zone, null);
    }

    public void sendCommand(String action, String Type, int Zone, Map<String, Object> data) {
        //should run command async here
        switch(action) {
            case "LightOn":
                this.onLightOn(Zone, Type);
                break;
            case "LightOff":
                this.onLightOff(Zone, Type);
                break;
            case "GlobalOn":
                this.onGlobalOn();
                break;
            case "GlobalOff":
                this.onGlobalOff();
                break;
            case "Brightness":
                this.onSetBrightness(Type, Zone, (float)data.get("value"));
                break;
            case "IncreaseBrightness":
                this.onIncreaseBrightness(Type, Zone);
                break;
            case "DecreaseBrightness":
                this.onDecreaseBrightness(Type, Zone);
                break;
            case "LightColor":
                this.onSetColor(Zone, (int)data.get("value"));
                break;
            case "Temperature":
                this.onSetTemperature(Type, Zone, (float)data.get("value"));
                break;
            case "IncreaseTemperature":
                this.onIncreaseTemperature(Type, Zone);
                break;
            case "DecreaseTemperature":
                this.onDecreaseTemperature(Type, Zone);
                break;
            case "LightNight":
                this.onSetNight(Type, Zone);
                break;
            case "LightFull":
                this.onSetFull(Type, Zone);
                break;
            case "LightWhite":
                this.onSetWhite(Zone);
                break;
        }
    }
}
