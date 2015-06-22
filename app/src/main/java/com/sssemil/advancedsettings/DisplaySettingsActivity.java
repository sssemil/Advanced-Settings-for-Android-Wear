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
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.util.ArrayList;
import java.util.List;


public class DisplaySettingsActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View prefsRoot = inflater.inflate(R.layout.activity_display_settings, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use preference.PreferenceScreen as its root element");
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("screen_saver_brightness_settings")) {
                if (Utils.isDeviceRooted()) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else {
                loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
            }
        }
        addPreferences(loadedPreferences);

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
            if(BuildConfig.DEBUG) {                         Log.d(TAG, "catch " + e.toString() + " hit in run", e);                     }
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
