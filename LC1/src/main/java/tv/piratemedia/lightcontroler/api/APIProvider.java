package tv.piratemedia.lightcontroler.api;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public class APIProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher;
    private MatrixCursor ZonesCursor;
    private SharedPreferences prefs;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI("tv.piratemedia.lightcontroler.api", "zones", 1);
        sUriMatcher.addURI("tv.piratemedia.lightcontroler.api", "zones/#", 2);
        sUriMatcher.addURI("tv.piratemedia.lightcontroler.api", "permission/*", 3);
    }

        @Override
    public boolean onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] zone = new String[5];
        zone[0] = "id";
        zone[1] = "name";
        zone[2] = "type";
        zone[3] = "global";
        zone[4] = "index";
        switch (sUriMatcher.match(uri)) {
            case 1:
                ZonesCursor = new MatrixCursor(zone);
                Object[] glz = new Object[5];
                glz[0] = 0;
                glz[1] = "All Color";
                glz[2] = "color";
                glz[3] = 1;
                glz[4] = 0;
                ZonesCursor.addRow(glz);
                glz[1] = "All White";
                glz[2] = "white";
                glz[4] = 9;
                ZonesCursor.addRow(glz);
                for(int i = 1; i < 9; i++) {
                    int id = i;
                    String type = "color";
                    if(i > 4) {
                        id = i - 4;
                        type = "white";
                    }
                    Object[] lz = new Object[5];
                    lz[0] = id;
                    lz[1] = prefs.getString("pref_zone"+i, "Zone "+id);
                    lz[2] = type;
                    lz[3] = 0;
                    lz[4] = i;
                    ZonesCursor.addRow(lz);
                }
                return ZonesCursor;
            case 2:
                int i = Integer.decode(uri.getLastPathSegment());

                int id = i;
                String type = "color";
                if(i > 4) {
                    id = i - 4;
                    type = "white";
                }

                MatrixCursor ZoneCursor = new MatrixCursor(zone);
                Object[] lz = new Object[5];
                lz[0] = id;
                lz[1] = prefs.getString("pref_zone"+i, "Zone "+id);
                lz[2] = type;
                lz[3] = 0;
                lz[4] = i;
                ZoneCursor.addRow(lz);

                return ZoneCursor;
            case 3:
                String[] perm = new String[2];
                perm[0] = "id";
                perm[1] = "allowed";

                Set<String> enabled = prefs.getStringSet("enabled_api_apps", new HashSet<String>());

                MatrixCursor PermCursor = new MatrixCursor(perm);
                Object[] lp = new Object[2];
                lp[0] = uri.getLastPathSegment();
                lp[1] = enabled.contains(uri.getLastPathSegment()) ? 1 : 0;

                PermCursor.addRow(lp);

                return PermCursor;
        }

        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                return "vnd.android.cursor.dir/vnd.tv.piratemedia.lightcontroler.api.zones";
            case 2:
                return "vnd.android.cursor.item/vnd.tv.piratemedia.lightcontroler.api.zones";
            case 3:
                return "vnd.android.cursor.item/vnd.tv.piratemedia.lightcontroler.api.permission";
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
