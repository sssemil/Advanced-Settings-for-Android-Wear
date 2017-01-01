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

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST = 123;
    //private AlertDialog mAlertDialog;
    //private boolean mContinue = false;
    //private boolean mContinueActivity = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    while (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST);

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST);

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_SETTINGS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_SETTINGS},
                                REQUEST);

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    while (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.SYSTEM_ALERT_WINDOW)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                                REQUEST);

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    /*if (!android.provider.Settings.System.canWrite(MainActivity.this)) {
                        mContinue = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAlertDialog = new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(getString(R.string.grant_access_settings))
                                        .setPositiveButton(getString(android.R.string.ok),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        mContinue = true;
                                                    }
                                                })
                                        .create();
                                mAlertDialog.show();
                            }
                        });

                        while (!mContinue) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));

                        // Start Activity
                        startActivityForResult(intent, REQUEST);

                        while (!mContinueActivity) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }*/
                }
            }
        }).start();

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
                if (Utils.isPackageInstalled("sssemil.com.languagesettingsprovider", this, 1)) {
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
