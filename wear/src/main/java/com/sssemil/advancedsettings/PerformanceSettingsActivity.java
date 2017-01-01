/*
 * Copyright (c) 2017 Emil Suleymanov <suleymanovemil8@gmail.com>
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
import android.view.View;

import com.sssemil.advancedsettings.util.DeviceCfg;
import com.sssemil.advancedsettings.util.preference.ListPreference;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sssemil.advancedsettings.util.DeviceCfg.getCurrentGovernor;
import static com.sssemil.advancedsettings.util.DeviceCfg.getMaxFrequency;
import static com.sssemil.advancedsettings.util.DeviceCfg.getMinFrequency;


public class PerformanceSettingsActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String GOVERNOR_PREF = "governor_settings";
    public static final String MIN_FREQ_PREF = "min_freq";
    public static final String MAX_FREQ_PREF = "max_freq";

    private static final String TAG = "PerformanceSettings";

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final View prefsRoot = inflater.inflate(R.layout.activity_performance_settings, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use" +
                    " preference.PreferenceScreen as its root element");
        }

        String currentGovernor = null;
        String currentMinFreq = null;
        String currentMaxFreq = null;

        try {
            currentGovernor = getCurrentGovernor();
            currentMinFreq = getMinFrequency();
            currentMaxFreq = getMaxFrequency();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            switch ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())) {
                case GOVERNOR_PREF:
                    try {
                        ArrayList<String> govsList = DeviceCfg.getAvailableGovernors();

                        CharSequence[] govs = new CharSequence[govsList.size()];

                        govs = govsList.toArray(govs);

                        ListPreference pref = (ListPreference) parsePreference(((PreferenceScreen)
                                prefsRoot).getChildAt(i));

                        pref.entries = pref.entryValues = govs;
                        pref.defaultValue = currentGovernor;

                        loadedPreferences.add(pref);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MIN_FREQ_PREF:
                    try {
                        ArrayList<String> freqsList = DeviceCfg.getAvailableFrequencies();

                        CharSequence[] freqs = new CharSequence[freqsList.size()];

                        freqs = freqsList.toArray(freqs);

                        ListPreference pref = (ListPreference) parsePreference(((PreferenceScreen)
                                prefsRoot).getChildAt(i));

                        pref.entries = pref.entryValues = freqs;
                        pref.defaultValue = currentMinFreq;

                        loadedPreferences.add(pref);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case MAX_FREQ_PREF:
                    try {
                        ArrayList<String> freqsList = DeviceCfg.getAvailableFrequencies();

                        CharSequence[] freqs = new CharSequence[freqsList.size()];

                        freqs = freqsList.toArray(freqs);

                        ListPreference pref = (ListPreference) parsePreference(((PreferenceScreen)
                                prefsRoot).getChildAt(i));

                        pref.entries = pref.entryValues = freqs;
                        pref.defaultValue = currentMaxFreq;

                        loadedPreferences.add(pref);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    loadedPreferences.add(parsePreference(((PreferenceScreen)
                            prefsRoot).getChildAt(i)));
                    break;
            }
        }
        addPreferences(loadedPreferences);

        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mSharedPreferences.edit().putString(GOVERNOR_PREF, currentGovernor)
                .putString(MIN_FREQ_PREF, currentMinFreq)
                .putString(MAX_FREQ_PREF, currentMaxFreq).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        try {
            switch (key) {
                case GOVERNOR_PREF:
                    try {
                        DeviceCfg.setCurrentGovernor(sharedPreferences.getString(key, null));
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case MIN_FREQ_PREF:
                    try {
                        DeviceCfg.setMinFrequency(sharedPreferences.getString(key, null));
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case MAX_FREQ_PREF:
                    try {
                        DeviceCfg.setMaxFrequency(sharedPreferences.getString(key, null));
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}