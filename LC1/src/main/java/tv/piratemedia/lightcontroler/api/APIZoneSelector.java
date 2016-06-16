package tv.piratemedia.lightcontroler.api;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import tv.piratemedia.lightcontroler.R;

public class APIZoneSelector extends Activity {
    private int selectedZone = -2;
    private String selectedType = "color";
    private boolean first = true;
    private boolean changing = false;

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

        final RadioGroup rg = (RadioGroup)findViewById(R.id.group);
        final RadioGroup rg2 = (RadioGroup)findViewById(R.id.group2);

        final RadioButton g = (RadioButton)findViewById(R.id.g);
        final RadioButton z1 = (RadioButton)findViewById(R.id.z1);
        final RadioButton z2 = (RadioButton)findViewById(R.id.z2);
        final RadioButton z3 = (RadioButton)findViewById(R.id.z3);
        final RadioButton z4 = (RadioButton)findViewById(R.id.z4);

        final RadioButton g2 = (RadioButton)findViewById(R.id.g2);
        final RadioButton z5 = (RadioButton)findViewById(R.id.z5);
        final RadioButton z6 = (RadioButton)findViewById(R.id.z6);
        final RadioButton z7 = (RadioButton)findViewById(R.id.z7);
        final RadioButton z8 = (RadioButton)findViewById(R.id.z8);

        z1.setText(prefs.getString("pref_zone1", "Zone 1"));
        z2.setText(prefs.getString("pref_zone2", "Zone 2"));
        z3.setText(prefs.getString("pref_zone3", "Zone 3"));
        z4.setText(prefs.getString("pref_zone4", "Zone 4"));

        z5.setText(prefs.getString("pref_zone5", "Zone 1"));
        z6.setText(prefs.getString("pref_zone6", "Zone 2"));
        z7.setText(prefs.getString("pref_zone7", "Zone 3"));
        z8.setText(prefs.getString("pref_zone8", "Zone 4"));

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(changing) {
                    changing = false;
                    return;
                }
                selectedType = "color";
                if (checkedId == R.id.g) {
                    selectedZone = 0;
                } else if (checkedId == R.id.z1) {
                    selectedZone = 1;
                } else if (checkedId == R.id.z2) {
                    selectedZone = 2;
                } else if (checkedId == R.id.z3) {
                    selectedZone = 3;
                } else if (checkedId == R.id.z4) {
                    selectedZone = 4;
                }

                if(first) {
                    first = false;
                } else {
                    changing = true;
                }

                g2.setChecked(false);
                z5.setChecked(false);
                z6.setChecked(false);
                z7.setChecked(false);
                z8.setChecked(false);
            }
        });
        rg2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(changing) {
                    changing = false;
                    return;
                }
                selectedType = "white";
                if(checkedId == R.id.g2) {
                    selectedZone = 0;
                } else if (checkedId == R.id.z5) {
                    selectedZone = 1;
                } else if (checkedId == R.id.z6) {
                    selectedZone = 2;
                } else if (checkedId == R.id.z7) {
                    selectedZone = 3;
                } else if (checkedId == R.id.z8) {
                    selectedZone = 4;
                }

                if(first) {
                    first = false;
                } else {
                    changing = true;
                }

                g.setChecked(false);
                z1.setChecked(false);
                z2.setChecked(false);
                z3.setChecked(false);
                z4.setChecked(false);
            }
        });
    }

    private void selctionComplete() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent i = new Intent();
        LightZone lz = new LightZone();
        lz.Name = selectedZone > 0 && selectedZone < 9 ? prefs.getString("pref_zone"+selectedZone, "Zone "+selectedZone) : getResources().getString(R.string.gloabl);
        lz.Type = selectedType;
        lz.ID = selectedZone;
        lz.Global = selectedZone > 0 && selectedZone < 9;
        i.putExtra("LightZone", (java.io.Serializable) lz);
        /*i.putExtra("zone", selectedZone);
        i.putExtra("type", selectedType);
        i.putExtra("name", selectedZone > 0 && selectedZone < 9 ? prefs.getString("pref_zone"+selectedZone, "Zone "+selectedZone) : getResources().getString(R.string.gloabl));*/
        setResult(RESULT_OK, i);
        finish();
    }

    private void dialogCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
