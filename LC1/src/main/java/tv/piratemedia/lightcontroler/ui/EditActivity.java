/*
 * Copyright 2013 two forty four a.m. LLC <http://www.twofortyfouram.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <http://www.apache.org/licenses/LICENSE-2.0>
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package tv.piratemedia.lightcontroler.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import tv.piratemedia.lightcontroler.DataTypes.TaskerCommand;
import tv.piratemedia.lightcontroler.R;
import tv.piratemedia.lightcontroler.bundle.BundleScrubber;
import tv.piratemedia.lightcontroler.bundle.PluginBundleManager;
//import com.instabug.library.Instabug;
//import com.instabug.wrapper.impl.v14.InstabugAnnotationActivity;
import com.larswerkman.holocolorpicker.ColorPicker;

import java.util.ArrayList;

/**
 * This is the "Edit" activity for a Locale Plug-in.
 * <p>
 * This Activity can be started in one of two states:
 * <ul>
 * <li>New plug-in instance: The Activity's Intent will not contain
 * {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE}.</li>
 * <li>Old plug-in instance: The Activity's Intent will contain
 * {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} from a previously saved plug-in instance that the
 * user is editing.</li>
 * </ul>
 *
 * @see com.twofortyfouram.locale.Intent#ACTION_EDIT_SETTING
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class EditActivity extends AbstractPluginActivity
{
    public boolean recreateView = false;
    private View cacheView = null;
    private static Context ctx;

    private ArrayList<TaskerCommand> commands = new ArrayList<TaskerCommand>();

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        BundleScrubber.scrub(getIntent());

        final Bundle localeBundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(localeBundle);

        setContentView(R.layout.tasker_setup);

        if (null == savedInstanceState)
        {
            if (PluginBundleManager.isBundleValid(localeBundle))
            {
                final String message = localeBundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);
            }
        }

        if(!recreateView) {
            final View rootView = getLayoutInflater().inflate(R.layout.rgbw_control, (ViewGroup) findViewById(R.id.main), false);

            SeekBar brightness = (SeekBar) findViewById(R.id.brightness);
            ToggleButton io = (ToggleButton) findViewById(R.id.onoff);
            Button white = (Button) findViewById(R.id.white);
            final ColorPicker color = (ColorPicker) findViewById(R.id.color);

            color.setShowOldCenterColor(false);
            color.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                @Override
                public void onColorChanged(int i) {
                    commands.add(new TaskerCommand(0, TaskerCommand.TASKTYPE.COLOR, i));
                    ToggleButton io = (ToggleButton) findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    commands.add(new TaskerCommand(0, TaskerCommand.TASKTYPE.BRIGHTNESS, progress));
                    ToggleButton io = (ToggleButton) findViewById(R.id.onoff);
                    io.setChecked(true);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            io.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        commands.add(new TaskerCommand(0, TaskerCommand.TASKTYPE.ON));
                    } else {
                        commands.add(new TaskerCommand(0, TaskerCommand.TASKTYPE.OFF));
                    }
                }
            });

            white.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    commands.add(new TaskerCommand(0, TaskerCommand.TASKTYPE.WHITE));
                    ToggleButton io = (ToggleButton) findViewById(R.id.onoff);
                    io.setChecked(true);
                }
            });

            recreateView = true;
            cacheView = rootView;
        }

        ctx = this;
    }

    @Override
    public void finish()
    {
        if (!isCanceled())
        {
            String message = "";
            for (int i = 0; i < commands.size(); i++)
                message += commands.get(i).toString() + ":";

            System.out.println(message);

            if (message.length() > 0)
            {
                final Intent resultIntent = new Intent();

                /*
                 * This extra is the data to ourselves: either for the Activity or the BroadcastReceiver. Note
                 * that anything placed in this Bundle must be available to Locale's class loader. So storing
                 * String, int, and other standard objects will work just fine. Parcelable objects are not
                 * acceptable, unless they also implement Serializable. Serializable objects must be standard
                 * Android platform objects (A Serializable class private to this plug-in's APK cannot be
                 * stored in the Bundle, as Locale's classloader will not recognize it).
                 */
                final Bundle resultBundle = PluginBundleManager.generateBundle(getApplicationContext(), message);
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

                /*
                 * The blurb is concise status text to be displayed in the host's UI.
                 */
                final String blurb = generateBlurb(getApplicationContext(), message);
                resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, blurb);

                setResult(RESULT_OK, resultIntent);
            }
        }

        super.finish();
    }

    /**
     * @param context Application context.
     * @param message The toast message to be displayed by the plug-in. Cannot be null.
     * @return A blurb for the plug-in.
     */
    /* package */static String generateBlurb(final Context context, final String message)
    {
        final int maxBlurbLength =
                context.getResources().getInteger(R.integer.twofortyfouram_locale_maximum_blurb_length);

        String out = "";

        String[] _in = message.split(":");
        String[][] in = new String[_in.length][3];
        for (int i = 0; i < in.length; i++) {
            in[i] = _in[i].split(";");
        }
        for (int i = 0; i < in.length; i++) {
            out += "Zone " + in[i][0];
            out += ", Task " + in[i][1];
            out += ", Data " + in[i][2] + "; ";
        }

        if (out.length() > maxBlurbLength)
        {
            return out.substring(0, maxBlurbLength);
        }

        return out;
    }
}