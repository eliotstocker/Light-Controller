package tv.piratemedia.lightcontroler.api;

import java.io.Serializable;

public class LightZone implements Serializable {
    public int ID;
    public String Name;
    public String Type;
    public boolean Global;

    public boolean isColor() {
        return Type.equals("color");
    }

    public boolean isWhite() {
        return Type.equals("white");
    }
}
