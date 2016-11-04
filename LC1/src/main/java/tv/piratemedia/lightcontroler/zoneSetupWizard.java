package tv.piratemedia.lightcontroler;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.heinrichreimersoftware.materialdrawer.structure.DrawerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eliotstocker on 22/10/2016.
 */

public class zoneSetupWizard extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_zonesetupwizard);

        controller.ControllerPager cp = new controller.ControllerPager(getSupportFragmentManager(), null);
        List<String> zoneList = new ArrayList<>();

        for(int i = 0; i < cp.getCount(); i++) {
            zoneList.add(cp.getPageTitle(i).toString());
        }

        ArrayAdapter<String> zones = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, zoneList);

        ListView lv = (ListView) findViewById(R.id.zonelist);
        lv.setAdapter(zones);
    }
}
