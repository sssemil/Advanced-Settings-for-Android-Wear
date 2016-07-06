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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import com.sssemil.advancedsettings.util.ShellUtils;
import com.sssemil.advancedsettings.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class MainService extends Service
        implements DisplayManager.DisplayListener, View.OnTouchListener {

    private static final String TAG = "MainService";

    private DisplayManager mDisplayManager;
    private SharedPreferences mSharedPreferences;
    private NotificationManager mNotificationManager;
    private boolean mShuttingDown = false;

    public static boolean isPhonePluggedIn(Context context){
        boolean charging = false;

        final Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = 0;
        if (batteryIntent != null) {
            status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        }
        boolean batteryCharge = status==BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = 0;
        if (batteryIntent != null) {
            chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        }
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        if (batteryCharge) charging=true;
        if (usbCharge) charging=true;
        if (acCharge) charging=true;

        return charging;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int id = R.mipmap.ic_cloud_outline_red;
            if (mSharedPreferences.getBoolean("alert_on_disconnect", false)
                    && !isPhonePluggedIn(MainService.this)) {
                Vibrator v = (Vibrator) MainService.this.getApplicationContext()
                        .getSystemService(Context.VIBRATOR_SERVICE);
                if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(MainService.this)
                                    .setSmallIcon(id)
                                    .setContentText(getString(R.string.forgot_device))
                                    .setLargeIcon(BitmapFactory.decodeResource(
                                            getResources(), R.drawable.notif_background));
                    if(!mShuttingDown) {
                        v.vibrate(new long[]{100, 600, 100, 600}, -1);
                        mNotificationManager.notify(id, mBuilder.build());
                    }
                } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                    v.vibrate(new long[]{100, 100, 100, 100}, -1);
                    mNotificationManager.cancel(id);
                }
            }
        }
    };

    private final BroadcastReceiver mReceiverOnPowerOff = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                int id = R.mipmap.ic_cloud_outline_red;
                mNotificationManager.cancel(id);
                mShuttingDown = true;
            }
        }
    };

    public MainService() {
    }

    //Useful stuff for feature stuff :P
    public static void grandPermissions(Context context) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String package_name = context.getPackageName();
        String[] commands = {"su", "-c", "\"pm", "grant", package_name
                + " android.permission.CHANGE_CONFIGURATION\""};
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

        filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        registerReceiver(mReceiverOnPowerOff, filter);

        int timeout = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString("screen_timeout_settings",
                        String.valueOf(Settings.System.getInt(getContentResolver(),
                                Settings.System.SCREEN_OFF_TIMEOUT, 0))));

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout);
        }

        String app = mSharedPreferences.getString("on_theatre_mode_launch_app", "null");

        if(!app.equals("null") && !Utils.isPackageInstalled(app, this, 0)) {
            mSharedPreferences.edit().putString("on_theatre_mode_launch_app", "null").apply();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                            Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(MainService.this)
                            .getString("screen_timeout_settings",
                                    String.valueOf(Settings.System.getInt(getContentResolver(),
                                            Settings.System.SCREEN_OFF_TIMEOUT, 0)))));

                } catch (SecurityException e){
                    e.printStackTrace();
                }
            }
        }).start();

        //language settings provider installer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //TODO update versionCode when it's updated
                    if (!Utils.isPackageInstalled("sssemil.com.languagesettingsprovider",
                            MainService.this, 6)) {
                        File apk = new File(Environment.getExternalStorageDirectory(),
                                "wear_languagesettingsprovider-release.apk");

                        if (apk.exists()) {
                            Log.i(TAG, "apk exists");
                            apk.delete();
                        }

                        InputStream inputStream = getAssets().open(
                                "wear_languagesettingsprovider-release.apk");

                        FileOutputStream file = new FileOutputStream(apk);
                        byte buf[] = new byte[4096];

                        int len = inputStream.read(buf);
                        while (len > 0) {
                            file.write(buf, 0, len);
                            len = inputStream.read(buf);
                        }
                        file.close();


                        if (apk.exists()) {
                            Log.i(TAG, "installing....");
                            ShellUtils.CommandResult result = ShellUtils.execCommand(
                                    "su -c pm install -r " + apk.getPath(), true);
                            if (result.errorMsg == null) {
                                Log.i(TAG, "done");
                            } else {
                                Log.e(TAG, "failed?");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //vibration intensity stuff
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

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                        int i = Settings.Global.getInt(getContentResolver(), "theater_mode_on");

                        Log.i("theater_mode_on", String.valueOf(i));
                    } catch (Settings.SettingNotFoundException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();*/

        GlobalContentObserver contentObserver = new GlobalContentObserver(new Handler());
        this.getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.Settings.Global.CONTENT_URI, true,
                contentObserver );
    }

    class GlobalContentObserver extends ContentObserver {

        private int THEATRE_MODE;

        public GlobalContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            try {
                int i = Settings.Global.getInt(getContentResolver(), "theater_mode_on");
                if(i != THEATRE_MODE) {
                    THEATRE_MODE = i;
                    if(i == 1) {
                        Log.i("theater_mode", "on");

                        String packageName = mSharedPreferences.getString("on_theatre_mode_launch_app", "null");

                        if(!packageName.equals("null")) {
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                            String className = launchIntent.getComponent().getClassName();

                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.setComponent(
                                    ComponentName.unflattenFromString(packageName + "/" + className));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
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
                        boolean do_brightness =
                                mSharedPreferences.getBoolean("manage_screen_saver_brightness_settings", false);

                        if (do_brightness) {
                            Log.i(TAG, "echo \"" +
                                    brightness + "\" > " +
                                    Utils.getDeviceCfg(MainService.this).brightnessPath);

                            Runtime rt = Runtime.getRuntime();
                            String[] commands = {"su", "-c", "echo \"" +
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
                        }
                    } catch (IOException | InterruptedException e) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                        }
                    }

                    /*PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "WakeLock");
                    wakeLock.acquire();*/
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}