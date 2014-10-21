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

package com.dudeofawesome.limitlessLED.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Locale;

import com.twofortyfouram.locale.PackageUtilities;
import com.dudeofawesome.limitlessLED.Constants;

/**
 * If the user tries to launch the plug-in via the "Open" button in Google Play, this will redirect the user
 * to Locale.
 */
public final class InfoActivity extends Activity
{
    /**
     * URI to Locale in the native version of the Google Play.
     */
    private static final String APP_STORE_URI =
            "market://details?id=%s&referrer=utm_source=%s&utm_medium=app&utm_campaign=plugin"; //$NON-NLS-1$

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final PackageManager manager = getPackageManager();

        final String compatiblePackage = PackageUtilities.getCompatiblePackage(manager, null);

        if (null != compatiblePackage)
        {
            // after this point, assume Locale-compatible package is installed
            Log.v(Constants.LOG_TAG,
                  String.format(Locale.US, "Locale-compatible package %s is installed", compatiblePackage)); //$NON-NLS-1$
            try
            {
                final Intent i = manager.getLaunchIntentForPackage(compatiblePackage);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            catch (final Exception e)
            {
                /*
                 * Under normal circumstances, this shouldn't happen. Potential causes would be a TOCTOU error
                 * where the application is uninstalled or the application enforcing permissions that it
                 * shouldn't be.
                 */
                Log.e(Constants.LOG_TAG, "Error launching Activity", e); //$NON-NLS-1$
            }
        }
        else
        {
            Log.i(Constants.LOG_TAG, "Locale-compatible package is not installed"); //$NON-NLS-1$

            try
            {
                startActivity(new Intent(
                                         Intent.ACTION_VIEW,
                                         Uri.parse(String.format(Locale.US,
                                                                 APP_STORE_URI,
                                                                 "com.twofortyfouram.locale", getPackageName()))).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)); //$NON-NLS-1$
            }
            catch (final Exception e)
            {
                Log.e(Constants.LOG_TAG, "Error launching Activity", e); //$NON-NLS-1$
            }
        }

        finish();
    }
}