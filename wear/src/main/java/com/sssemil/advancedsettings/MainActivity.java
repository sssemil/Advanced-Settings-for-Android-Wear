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

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.edit().putString("system_language",
                Locale.getDefault().getLanguage().toLowerCase() + "-" + Locale.getDefault().getCountry().toUpperCase()).apply();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final View prefsRoot = inflater.inflate(R.layout.preferences, null);

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
                loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
            } else if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("system_language")) {
                if(Utils.isPackageInstalled("sssemil.com.languagesettingsprovider", this, 1)) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else {
                loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
            }
        }
        addPreferences(loadedPreferences);

        this.startService(new Intent(this, MainService.class));

        if (!Utils.isDeviceRooted()) {
            Dialog dialog = new Dialog(this);
            dialog.setTitle("Warning");
            dialog.setContentView(R.layout.warning);
            dialog.show();
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals("system_language")) {
            Intent intent = new Intent("sssemil.com.languagesettingsprovider.CHANGE_LOCALE");
            // add data
            intent.putExtra("locale", sharedPreferences.getString(key, ""));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            /*new  Thread(new Runnable() {
                @Override
                public void run() {
                    String[] line = sharedPreferences.getString(key, "").split("-");
                    String lang = line[0].toLowerCase();
                    String cont = line[1].toUpperCase();
                    try {
                        Utils.updateLocale(lang, cont);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/
        }
    }
}
