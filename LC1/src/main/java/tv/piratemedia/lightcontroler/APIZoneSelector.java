package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class APIZoneSelector extends Activity {
    private int selectedZone = -2;
    private String selectedType = "color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.api_zone_selector);

        Button cancel = (Button) findViewById(R.id.cancel);
        Button select = (Button) findViewById(R.id.select);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCancel();
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selctionComplete();
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        RadioButton z1 = (RadioButton)findViewById(R.id.z1);
        RadioButton z2 = (RadioButton)findViewById(R.id.z2);
        RadioButton z3 = (RadioButton)findViewById(R.id.z3);
        RadioButton z4 = (RadioButton)findViewById(R.id.z4);
        RadioGroup rg = (RadioGroup)findViewById(R.id.group);

        z1.setText(prefs.getString("pref_zone1", "Zone 1"));
        z2.setText(prefs.getString("pref_zone2", "Zone 2"));
        z3.setText(prefs.getString("pref_zone3", "Zone 3"));
        z4.setText(prefs.getString("pref_zone4", "Zone 4"));

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.g) {
                    selectedZone =  0;
                } else if (checkedId == R.id.z1) {
                    selectedZone =  1;
                } else if (checkedId == R.id.z2) {
                    selectedZone =  2;
                } else if (checkedId == R.id.z3) {
                    selectedZone =  3;
                } else if (checkedId == R.id.z4) {
                    selectedZone =  4;
                }
            }
        });
    }

    private void selctionComplete() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent i = new Intent();
        i.putExtra("zone", selectedZone);
        i.putExtra("type", selectedType);
        i.putExtra("name", selectedZone > 0 ? prefs.getString("pref_zone"+selectedZone, "Zone "+selectedZone) : getResources().getString(R.string.gloabl));
        setResult(RESULT_OK, i);
        finish();
    }

    private void dialogCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
