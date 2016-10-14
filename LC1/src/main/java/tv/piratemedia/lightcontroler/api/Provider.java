package tv.piratemedia.lightcontroler.api;

/**
 * Created by jetoapple on 14/10/2016.
 */
public class Provider {
    public String Name;
    public String Package;
    public String Provider;
    public boolean ColorBrightnessStatefull = false;
    public boolean WhiteBrightnessStatefull = false;
    public boolean ColorHasTemperature = false;
    public boolean ColorTemperatureStatefull = false;
    public boolean WhiteTemperatureStatefull = false;

    public Provider(String Name, String Package, String Provider) {
        this.Name = Name;
        this.Package = Package;
        this.Provider = Provider;
    }
}
