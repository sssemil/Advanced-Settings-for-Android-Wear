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
import android.view.MotionEvent;
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainService extends Service
        implements DisplayManager.DisplayListener, View.OnTouchListener {

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

    private static final String TAG = "MainService";

    private DisplayManager mDisplayManager;

    private SharedPreferences mSharedPreferences;

    private NotificationManager mNotificationManager;

    public MainService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i("Touch", "Clicked");
        return false;
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

        int timeout = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("screen_timeout_settings",
                        String.valueOf(Settings.System.getInt(getContentResolver(),
                                Settings.System.SCREEN_OFF_TIMEOUT, 0))));

        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(MainService.this)
                        .getString("screen_timeout_settings",
                                String.valueOf(Settings.System.getInt(getContentResolver(),
                                        Settings.System.SCREEN_OFF_TIMEOUT, 0)))));

            }
        }).start();

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
        try {
            mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            mDisplayManager.registerDisplayListener(MainService.this, null);
            switch (mDisplayManager.getDisplay(displayId).getState()) {
                case Display.STATE_DOZE_SUSPEND:
                case Display.STATE_DOZE:
                    try {
                        //TODO: Need to fix screen_saver_brightness_settings. SELinux problems?
                        Thread.sleep(3);
                        int brightness = Integer.parseInt(
                                mSharedPreferences.getString("screen_saver_brightness_settings",
                                        String.valueOf(Utils.getDeviceCfg(MainService.this).brightnessDefault)));

                        Process p = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(p.getOutputStream());
                        os.writeBytes("echo \"" +
                                brightness + "\" > " +
                                Utils.getDeviceCfg(MainService.this).brightnessPath);
                        os.writeBytes("exit\n");
                        os.flush();

                        Log.i(TAG, "echo \"" +
                                brightness + "\" > " +
                                Utils.getDeviceCfg(MainService.this).brightnessPath);

                        Runtime rt = Runtime.getRuntime();
                        String[] commands = {"su","-v", "echo \"" +
                                brightness + "\" > " +
                                Utils.getDeviceCfg(MainService.this).brightnessPath};
                        Process proc = rt.exec(commands);

                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(proc.getInputStream()));

                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(proc.getErrorStream()));

                        String s;
                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                        }
                        while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                        }
                    } catch (IOException | InterruptedException e) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}