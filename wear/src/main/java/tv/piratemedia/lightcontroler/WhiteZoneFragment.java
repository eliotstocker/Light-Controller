package tv.piratemedia.lightcontroler;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class WhiteZoneFragment extends ZoneFragment {
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ZoneFragment newInstance(int sectionNumber, String zoneName) {
        return ZoneFragment.newInstance(new WhiteZoneFragment(), sectionNumber, zoneName);
    }
}
