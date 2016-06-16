package tv.piratemedia.lightcontroler.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashSet;
import java.util.Set;

import tv.piratemedia.lightcontroler.R;

public class APIPermissionManager extends ActionBarActivity {
    private SharedPreferences prefs;
    private ListView PermsList;
    private ListAdapter PermsAdapter;
    private Set<String> enabled;
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_permission_manager);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PermsList = (ListView) findViewById(R.id.permission_list);

        if(Build.VERSION.SDK_INT == 21) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mActionBarToolbar);
        mActionBarToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APIPermissionManager.this.finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        enabled = prefs.getStringSet("enabled_api_apps", new HashSet<String>());

        setUpAdapter();

        final Context _this = this;
        PermsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Application a = (Application)PermsAdapter.getItem(position);
                new MaterialDialog.Builder(_this)
                        .title("Remove Permission")
                        .content("Remove API Permission for '"+a.Name+"'?")
                        .cancelable(true)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                enabled.remove(a.pkg);
                                setUpAdapter();
                                prefs.getStringSet("enabled_api_apps", enabled);
                                _this.getContentResolver().notifyChange(Uri.parse("content://tv.piratemedia.lightcontroler.api/permission"), null);
                            }
                        })
                        .positiveText("Remove")
                        .negativeText("Cancel")
                        .build()
                        .show();
            }
        });
    }

    private void setUpAdapter() {
        final Context _this = this;
        PermsAdapter = new ListAdapter() {
            ViewHolder holder;
            @Override
            public boolean areAllItemsEnabled() {
                return true;
            }

            @Override
            public boolean isEnabled(int position) {
                return true;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return enabled.size();
            }

            @Override
            public Application getItem(int position) {
                Application item = new Application();
                item.pkg = (String) enabled.toArray()[position];
                final PackageManager pm = _this.getPackageManager();
                ApplicationInfo ai;
                try {
                    ai = pm.getApplicationInfo(item.pkg, 0);
                } catch (final PackageManager.NameNotFoundException e) {
                    ai = null;
                }
                if(ai != null) {
                    item.Icon = pm.getApplicationIcon(ai);
                    item.Name = (String) pm.getApplicationLabel(ai);
                }
                return item;
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
                Application item = getItem(position);
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) _this
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.permission_item, null);

                    holder = new ViewHolder();
                    holder.name = (TextView) convertView.findViewById(R.id.app_name);
                    holder.pkg = (TextView) convertView.findViewById(R.id.pkg_id);
                    holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);

                    if(item.Name != null) {
                        holder.name.setText(item.Name);
                    } else {
                        holder.name.setText("App Not Installed");
                    }
                    holder.pkg.setText(item.pkg);
                    if(item.Icon != null) {
                        holder.icon.setImageDrawable(item.Icon);
                    }

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();

                    holder.name.setText(item.Name);
                    holder.pkg.setText(item.pkg);
                    holder.icon.setImageDrawable(item.Icon);
                }
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
        PermsList.setAdapter(PermsAdapter);
    }

    class Application {
        public String pkg = null;
        public String Name = null;
        public Drawable Icon = null;
    }

    public static class ViewHolder {
        public TextView name;
        public TextView pkg;
        public ImageView icon;
    }
}
