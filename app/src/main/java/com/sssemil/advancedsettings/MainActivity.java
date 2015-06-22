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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View prefsRoot = inflater.inflate(R.layout.preferences, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use preference.PreferenceScreen as its root element");
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("wifi_settings")) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("bluetooth_setting")) {
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("power_settings")) {
                if (Utils.isDeviceRooted()) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else {
                loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
            }
        }
        addPreferences(loadedPreferences);

        this.startService(new Intent(this, MainService.class));
    }
}
