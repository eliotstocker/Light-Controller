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

package com.dudeofawesome.limitlessLED.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import com.dudeofawesome.limitlessLED.Constants;
import com.dudeofawesome.limitlessLED.DataTypes.TaskerCommand;
import com.dudeofawesome.limitlessLED.bundle.BundleScrubber;
import com.dudeofawesome.limitlessLED.bundle.PluginBundleManager;
import com.dudeofawesome.limitlessLED.controlCommands;
import com.dudeofawesome.limitlessLED.ui.EditActivity;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 *
 * @see com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING
 * @see com.twofortyfouram.locale.Intent#EXTRA_BUNDLE
 */
public final class FireReceiver extends BroadcastReceiver
{

    /**
     * @param context {@inheritDoc}.
     * @param intent the incoming {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING} Intent. This
     *            should contain the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by
     *            {@link EditActivity} and later broadcast by Locale.
     */
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        /*
         * Always be strict on input parameters! A malicious third-party app could send a malformed Intent.
         */

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
            if (Constants.IS_LOGGABLE)
            {
                Log.e(Constants.LOG_TAG,
                      String.format(Locale.US, "Received unexpected Intent action %s", intent.getAction())); //$NON-NLS-1$
            }
            return;
        }

        BundleScrubber.scrub(intent);

        final Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        BundleScrubber.scrub(bundle);

        if (PluginBundleManager.isBundleValid(bundle))
        {
            final String message = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_MESSAGE);

            controlCommands Controller;
            Controller = new controlCommands(context);

            String[] _in = message.split(":");
            String[][] in = new String[_in.length][3];
            for (int i = 0; i < in.length; i++) {
                in[i] = _in[i].split(";");
            }
            for (int i = in.length - 1; i >= 0; i++) {
                TaskerCommand.TASKTYPE task = TaskerCommand.TASKTYPE.values()[Integer.parseInt(in[i][1])];
                if (task == TaskerCommand.TASKTYPE.ON) {
                    Controller.LightsOn(Integer.parseInt(in[i][0]));
                }
                else if (task == TaskerCommand.TASKTYPE.OFF) {
                    Controller.LightsOff(Integer.parseInt(in[i][0]));
                }
                else if (task == TaskerCommand.TASKTYPE.WHITE) {
                    Controller.setToWhite(Integer.parseInt(in[i][0]));
                }
                else if (task == TaskerCommand.TASKTYPE.COLOR) {
                    Controller.setColor(Integer.parseInt(in[i][0]), Integer.parseInt(in[i][2]));
                }
                else if (task == TaskerCommand.TASKTYPE.BRIGHTNESS) {
                    Controller.setBrightness(Integer.parseInt(in[i][0]), Integer.parseInt(in[i][2]));
                }
            }
        }
    }
}