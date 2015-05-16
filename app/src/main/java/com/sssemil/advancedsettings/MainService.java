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

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;

import com.sssemil.advancedsettings.util.Utils;

import java.io.IOException;

public class MainService extends Service implements DisplayManager.DisplayListener {

    private static final String TAG = "MainService";

    private DisplayManager mDisplayManager;

    private SharedPreferences mSharedPreferences;

    private NotificationManager mNotificationManager;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (mSharedPreferences.contains("alert_on_disconnect")
                    && mSharedPreferences.getBoolean("alert_on_disconnect", true)) {
                if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(MainService.this)
                                    .setSmallIcon(R.mipmap.ic_cloud_outline)
                                    .setContentTitle(getString(R.string.forgot_device));
                    Vibrator v = (Vibrator) MainService.this.getApplicationContext()
                            .getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {100, 600, 100, 600};
                    v.vibrate(pattern, -1);
                    mNotificationManager.notify(R.mipmap.ic_cloud_outline, mBuilder.build());
                } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    mNotificationManager.cancel(R.mipmap.ic_cloud_outline);
                }
            }
        }
    };

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);

        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("screen_timeout_settings",
                                String.valueOf(Settings.System.getInt(getContentResolver(),
                                        Settings.System.SCREEN_OFF_TIMEOUT, 0)))));
        try {
            int amp = Integer.parseInt(
                    mSharedPreferences.getString("vibration_intensity",
                            String.valueOf(
                                    Utils.getDeviceCfg(MainService.this).vibroIntensetyDefault)));
            ProcessBuilder pb
                    = new ProcessBuilder("su", "-c", "echo",
                    amp + ">",
                    Utils.getDeviceCfg(MainService.this).vibroIntensetyPath);
            pb.start().waitFor();

            Vibrator v = (Vibrator) MainService.this.getApplicationContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1500);
        } catch (InterruptedException | IOException | NullPointerException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDisplayAdded(int displayId) {

    }

    @Override
    public void onDisplayRemoved(int displayId) {

    }

    @Override
    public void onDisplayChanged(int displayId) {
        //TODO: Make it less disgusting
        switch (mDisplayManager.getDisplay(displayId).getState()) {
            case Display.STATE_DOZE_SUSPEND:
            case Display.STATE_DOZE:
                try {
                    Thread.sleep(2);
                    int brightness = Integer.parseInt(
                            mSharedPreferences.getString("screen_saver_brightness_settings", null));

                    ProcessBuilder pb
                            = new ProcessBuilder("su", "-c", "echo",
                            brightness + ">",
                            Utils.getDeviceCfg(MainService.this).brightnessPath);
                    pb.start();
                } catch (IOException | InterruptedException e) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                }
                break;
            default:
                break;
        }
    }
}