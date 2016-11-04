package tv.piratemedia.lightcontroler;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

/**
 * Created by eliotstocker on 22/10/2016.
 */

public class zoneSetup extends ActionBarActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonesetup);

        if(!getIntent().hasExtra("type") || !getIntent().hasExtra("zone")) {
            finish();
            return;
        }

        String Type = getIntent().getStringExtra("type");
        int Zone = getIntent().getIntExtra("zone", -1);

        final controller.ControllerPager cp = new controller.ControllerPager(getSupportFragmentManager(), null, this);

        TextView info = (TextView) findViewById(R.id.info);

        String title = "Unknown";
        if(getIntent().hasExtra("index")) {
            title = cp.getPageTitle(getIntent().getIntExtra("index", 0)).toString();
        } else {
            title = cp.getPageTitle(cp.getIndex(Type, Zone)).toString();
        }
        info.setText("Setup " + Type + " Zone: " + title);
    }
}
