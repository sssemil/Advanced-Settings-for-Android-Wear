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
import android.bluetooth.BluetoothAdapter;
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

import java.io.IOException;

public class MainService extends Service implements DisplayManager.DisplayListener {

    private static final String TAG = "MainService";

    private DisplayManager mDisplayManager;

    private SharedPreferences mSharedPreferences;

    private NotificationManager mNotificationManager;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "STARTED!!!");

        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BuildConfig.DEBUG) {
                Log.i("BLUETOOTH", action);
            }
            if(mSharedPreferences.contains("alert_on_disconnect")
                    && mSharedPreferences.getBoolean("alert_on_disconnect", true)) {
                if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(MainService.this)
                                .setSmallIcon(R.mipmap.ic_cloud_outline)
                                .setContentTitle(getString(R.string.forgot_device));
                    Vibrator v = (Vibrator) MainService.this.getApplicationContext()
                                    .getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {100, 600, 100, 600};
                    v.vibrate(pattern, -1);
                    mNotificationManager.notify(R.mipmap.ic_cloud_outline, mBuilder.build());
                } else if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    mNotificationManager.cancel(R.mipmap.ic_cloud_outline);
                }

            }
        }
    };

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
                    int brightness = Settings.System.getInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, 0) - 25;
                    if (brightness < 10) {
                        brightness = 10;
                    }
                    ProcessBuilder pb
                            = new ProcessBuilder("su", "-c", "echo",
                            brightness + ">",
                            "/sys/class/leds/lcd-backlight/brightness");
                    pb.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }
}