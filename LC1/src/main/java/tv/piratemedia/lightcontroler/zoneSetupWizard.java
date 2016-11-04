package tv.piratemedia.lightcontroler;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eliotstocker on 22/10/2016.
 */

public class zoneSetupWizard extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonesetupwizard);

        final controller.ControllerPager cp = new controller.ControllerPager(getSupportFragmentManager(), null, this);
        List<String> zoneList = new ArrayList<>();

        Log.d("Zones", "Count: " + cp.getCount());
        for(int i = 0; i < cp.getCount(); i++) {
            zoneList.add(cp.getPageTitle(i).toString());
        }

        ArrayAdapter<String> zones = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, zoneList);

        ListView lv = (ListView) findViewById(R.id.zonelist);
        lv.setAdapter(zones);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cp.setupZone(i);
            }
        });
    }
}
