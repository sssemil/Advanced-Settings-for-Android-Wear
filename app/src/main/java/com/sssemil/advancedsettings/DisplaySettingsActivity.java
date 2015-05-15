/*
 * Copyright (c) 2015 Emil Suleymanov <suleymanovemil8@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.sssemil.advancedsettings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.sssemil.advancedsettings.preference.WearPreferenceActivity;


public class DisplaySettingsActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_display_settings);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        sharedPreferences.edit().putString("screen_timeout_settings",
                String.valueOf(Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, 0))).apply();

        sharedPreferences.edit().putString("brightness_settings",
                String.valueOf(Settings.System.getInt(getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 0))).apply();

        /*try {
            Context myContext
                    = this.createPackageContext("com.google.android.wearable.app",
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            Log.i("tilt_to_wake", String.valueOf(myContext.getApplicationInfo()));
            Log.i("tilt_to_wake", String.valueOf(Utils.tiltToWakeEnabled(myContext)));
        } catch (PackageManager.NameNotFoundException e) {
            if(BuildConfig.DEBUG) {                         e.printStackTrace();                     }
        }*/
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("screen_timeout_settings")
                && (Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 0)
                != Integer.parseInt(sharedPreferences.getString(key, null)))) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                    Integer.parseInt(sharedPreferences.getString(key, null)));
        } else if (key.equals("brightness_settings")
                && (Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 0)
                != Integer.parseInt(sharedPreferences.getString(key, null)))) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                    Integer.parseInt(sharedPreferences.getString(key, null)));
        }
    }
}
