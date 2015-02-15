package tv.piratemedia.lightcontroler;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

public class APIProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher;
    private SharedPreferences prefs;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI("tv.piratemedia.lightcontroler.api", "zones", 1);
        sUriMatcher.addURI("tv.piratemedia.lightcontroler.api", "zones/#", 2);
        sUriMatcher.addURI("tv.piratemedia.lightcontroler.api", "permission", 3);
    }

        @Override
    public boolean onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sUriMatcher.match(uri)) {
            case 1:

                break;
            case 2:

                break;
            case 3:

                break;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
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
