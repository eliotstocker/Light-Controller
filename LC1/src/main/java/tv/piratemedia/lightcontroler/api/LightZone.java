package tv.piratemedia.lightcontroler.api;

import java.io.Serializable;

public class LightZone implements Serializable {
    private static final long serialVersionUID = 7541824072245303498L;

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

    public boolean isSuperGlobal() {
        return Type.equals("super");
    }

}
