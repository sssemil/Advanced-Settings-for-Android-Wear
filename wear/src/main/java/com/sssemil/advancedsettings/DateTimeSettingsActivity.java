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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class DateTimeSettingsActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Calendar mCalendar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View prefsRoot = inflater.inflate(R.layout.activity_datetime_settings, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use preference.PreferenceScreen as its root element");
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            /*if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("hour24_settings")) {
                if (is24()) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("hour12_settings")) {
                if(!is24()) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("hour12ampm_settings")) {
                if(!is24()) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else {*/
            loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
            //}
        }
        addPreferences(loadedPreferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mCalendar = Calendar.getInstance();

        sharedPreferences.edit().putString("year_settings",
                String.valueOf(mCalendar.get(Calendar.YEAR))).apply();
        sharedPreferences.edit().putString("month_settings",
                String.valueOf(mCalendar.get(Calendar.MONTH) + 1)).apply();
        sharedPreferences.edit().putString("day_settings",
                String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH))).apply();
        //if(is24()) {
        sharedPreferences.edit().putString("hour24_settings",
                String.valueOf(mCalendar.get(Calendar.HOUR_OF_DAY))).apply();
        /*} else {
            sharedPreferences.edit().putString("hour12_settings",
                    String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))).apply();
            sharedPreferences.edit().putString("hour12ampm_settings",
                    String.valueOf(calendar.get(Calendar.AM_PM))).apply();

        }*/
        sharedPreferences.edit().putString("minute_settings",
                String.valueOf(mCalendar.get(Calendar.MINUTE))).apply();
        sharedPreferences.edit().putString("seconds_settings",
                String.valueOf(mCalendar.get(Calendar.SECOND))).apply();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mCalendar.getTimeInMillis();
        switch (key) {
            case "year_settings":
                changeSystemTime(sharedPreferences.getString(key, null),
                        String.valueOf(mCalendar.get(Calendar.MONTH) + 1),
                        String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)),
                        String.valueOf(mCalendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(mCalendar.get(Calendar.MINUTE)),
                        String.valueOf(mCalendar.get(Calendar.SECOND)));
                break;
            case "month_settings":
                changeSystemTime(String.valueOf(mCalendar.get(Calendar.YEAR)),
                        sharedPreferences.getString(key, null),
                        String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)),
                        String.valueOf(mCalendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(mCalendar.get(Calendar.MINUTE)),
                        String.valueOf(mCalendar.get(Calendar.SECOND)));
                break;
            case "day_settings":
                changeSystemTime(String.valueOf(mCalendar.get(Calendar.YEAR)),
                        String.valueOf(mCalendar.get(Calendar.MONTH) + 1),
                        sharedPreferences.getString(key, null),
                        String.valueOf(mCalendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(mCalendar.get(Calendar.MINUTE)),
                        String.valueOf(mCalendar.get(Calendar.SECOND)));
                break;
            case "hour24_settings":
                changeSystemTime(String.valueOf(mCalendar.get(Calendar.YEAR)),
                        String.valueOf(mCalendar.get(Calendar.MONTH) + 1),
                        String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)),
                        sharedPreferences.getString(key, null),
                        String.valueOf(mCalendar.get(Calendar.MINUTE)),
                        String.valueOf(mCalendar.get(Calendar.SECOND)));
                break;
            case "minute_settings":
                changeSystemTime(String.valueOf(mCalendar.get(Calendar.YEAR)),
                        String.valueOf(mCalendar.get(Calendar.MONTH) + 1),
                        String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)),
                        String.valueOf(mCalendar.get(Calendar.HOUR_OF_DAY)),
                        sharedPreferences.getString(key, null),
                        String.valueOf(mCalendar.get(Calendar.SECOND)));
                break;
            case "seconds_settings":
                changeSystemTime(String.valueOf(mCalendar.get(Calendar.YEAR)),
                        String.valueOf(mCalendar.get(Calendar.MONTH) + 1),
                        String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH)),
                        String.valueOf(mCalendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(mCalendar.get(Calendar.MINUTE)),
                        sharedPreferences.getString(key, null));
                break;
        }
    }

    public boolean is24() {
        return DateFormat.is24HourFormat(this);
    }

    private String clean(String inp) {
        if (inp.length() == 2) {
            return inp;
        } else if (inp.length() == 1) {
            return "0" + inp;
        } else return inp;
    }

    private void changeSystemTime(final String year, final String month,
                                  final String day, final String hour,
                                  final String minute, final String second) {

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String command = "date -s " + year
                    + clean(month) + clean(day)
                    + "." + clean(hour) + clean(minute) + clean(second) + "\n";
            Log.e("command", command);
            os.writeBytes(command);
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
