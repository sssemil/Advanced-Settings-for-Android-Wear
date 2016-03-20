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
package sssemil.com.languagesettingsprovider;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.android.app.IActivityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sssemil.com.languagesettingsprovider.preference.Preference;
import sssemil.com.languagesettingsprovider.preference.PreferenceScreen;
import sssemil.com.languagesettingsprovider.preference.WearPreferenceActivity;

public class MainActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Requests the system to update the system locale. Note that the system looks halted
     * for a while during the Locale migration, so the caller need to take care of it.
     * <p/>
     * Requires android.permission.CHANGE_CONFIGURATION
     */
    public static void updateLocale(Locale locale) {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();

            Configuration config = am.getConfiguration();

            // Will set userSetLocale to indicate this isn't some passing default - the user
            // wants this remembered
            config.setLocale(locale);

            am.updateConfiguration(config);
            // Trigger the dirty bit for the Settings Provider.
            BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
            // Intentionally left blank
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.edit().putString("system_language",
                Locale.getDefault().getLanguage().toLowerCase() + "-" + Locale.getDefault().getCountry().toUpperCase()).apply();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final View prefsRoot = inflater.inflate(R.layout.preferences, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use preference.PreferenceScreen as its root element");
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
        }
        addPreferences(loadedPreferences);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals("system_language")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String[] line = sharedPreferences.getString(key, "").split("-");
                    String lang = line[0].toLowerCase();
                    String cont = line[1].toUpperCase();
                    Locale locale = new Locale(lang, cont);
                    try {
                        grandPermissions(MainActivity.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateLocale(locale);
                }
            }).start();
        }
    }

    public static void grandPermissions(Context context) throws IOException {
        String package_name = context.getPackageName();
        ShellUtils.CommandResult result = ShellUtils.execCommand(
                "pm grant " + package_name + " android.permission.CHANGE_CONFIGURATION", true);

        try {
            Log.i("RESULTsuccessMsg:", result.successMsg);
            Log.i("RESULTerrorMsg:", result.errorMsg);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
