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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.ListPreference;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.util.ArrayList;
import java.util.List;


public class TasksActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final View prefsRoot = inflater.inflate(R.layout.activity_tasks, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use" +
                    " preference.PreferenceScreen as its root element");
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            switch ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())) {
                case "on_theatre_mode_launch_app":
                    ListPreference p = parseListPreference(((PreferenceScreen)
                            prefsRoot).getChildAt(i));
                    List listAppInfo = Utils.getAllApps(this);
                    PackageManager mPm = getPackageManager();
                    CharSequence[] entryValues = new CharSequence[listAppInfo.size() + 1];
                    CharSequence[] entries = new CharSequence[listAppInfo.size() + 1];
                    entryValues[0] = "null";
                    entries[0] = "null";
                    for (int n = 1; n < listAppInfo.size() + 1; n++) {
                        ApplicationInfo entry = (ApplicationInfo) listAppInfo.get(n - 1);
                        entries[n] = entry.loadLabel(mPm);
                        entryValues[n] = entry.packageName;
                    }
                    p.entries = entries;
                    p.entryValues = entryValues;
                    loadedPreferences.add(p);
                    break;
                default:
                    loadedPreferences.add(parsePreference(((PreferenceScreen)
                            prefsRoot).getChildAt(i)));
                    break;
            }
        }

        addPreferences(loadedPreferences);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            if (key.equals("on_theatre_mode_launch_app")) {

            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
