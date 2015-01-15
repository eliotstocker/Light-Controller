package tv.piratemedia.lightcontroler;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Map;


public class alarmPreferences extends Activity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_preferences);

        ListView AlarmList = (ListView)this.findViewById(R.id.alarmlist);
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        ListAdapter Alarms = new ListAdapter() {

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                int count = 0;
                String[] Keys = (String[]) prefs.getAll().keySet().toArray();
                Map<String, ?> Prefs = prefs.getAll();
                for(int i = 0; i < Prefs.size(); i++) {
                    if (Keys[i].startsWith("light-alarm-days")) {
                        count++;
                    } else if(Keys[i].startsWith("light-alarm-date")) {
                        count++;
                    }
                }
                return count;
            }

            @Override
            public Object getItem(int position) {
                int count = 0;
                String[] Keys = (String[]) prefs.getAll().keySet().toArray();
                Map<String, ?> Prefs = prefs.getAll();
                for(int i = 0; i < Prefs.size(); i++) {
                    if(count == position)
                    if (Keys[i].startsWith("light-alarm-days")) {

                        count++;
                    } else if(Keys[i].startsWith("light-alarm-date")) {
                        count++;
                    }
                }
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater)alarmPreferences.this.getApplicationContext().getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.alarm_item_layout, null);
                TextView Time = (TextView)convertView.findViewById(R.id.time);
                Switch enable = (Switch)convertView.findViewById(R.id.alarmon);
                ToggleButton mon = (ToggleButton)convertView.findViewById(R.id.togglemon);
                ToggleButton tue = (ToggleButton)convertView.findViewById(R.id.toggletue);
                ToggleButton wed = (ToggleButton)convertView.findViewById(R.id.togglewed);
                ToggleButton thu = (ToggleButton)convertView.findViewById(R.id.togglethu);
                ToggleButton fri = (ToggleButton)convertView.findViewById(R.id.togglefri);
                ToggleButton sat = (ToggleButton)convertView.findViewById(R.id.togglesat);
                ToggleButton sun = (ToggleButton)convertView.findViewById(R.id.togglesun);

                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 1;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.alarm_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
