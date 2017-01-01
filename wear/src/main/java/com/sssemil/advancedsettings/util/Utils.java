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
package com.sssemil.advancedsettings.util;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Utils {
    private static final String TAG = "A.S. Utils";

    //used to execute commands as root, returns list of command output
    public static ArrayList<String> exec(String command) {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{command});

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            ArrayList<String> list = new ArrayList<>();

            String s;
            while ((s = stdInput.readLine()) != null) {
                list.add(s);
                Log.i("EXEC", s);
            }

            while ((s = stdError.readLine()) != null) {
                list.add(s);
                Log.e("EXEC", s);
            }
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean tiltToWakeEnabled(Context paramContext) {
        return paramContext.getSharedPreferences("home_preferences", 0).getBoolean("tilt_to_wake", false);
    }

    /*
     * Get all installed application on mobile and return a list
     * @param   c   Context of application
     * @return  list of installed applications
     */
    public static List getAllApps(Context context) {
        return context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public static List getRunningServices(Context context) {
        List<ActivityManager.RunningServiceInfo> listInitial = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningServices(1000);
        List<ActivityManager.RunningServiceInfo> listNew = new ArrayList<>();

        for (int i = 0; i < listInitial.size(); i++) {
            boolean do_add = true;
            for (int n = 0; n < listNew.size(); n++) {
                if (listNew.get(n).service.getPackageName().equals(listInitial.get(i).service.getPackageName())) {
                    do_add = false;
                }
            }
            if (do_add) {
                listNew.add(listInitial.get(i));
            }
        }

        return listNew;
    }

    public static List getSystemApps(Context context) {
        List<ApplicationInfo> list
                = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> list_out = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo entry = list.get(i);
            if ((entry.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                list_out.add(list.get(i));
            }
        }

        return list_out;
    }

    public static List getDisabledSystemApps(Context context) {
        List<ApplicationInfo> list
                = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> list_out = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo entry = list.get(i);
            if ((entry.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && !entry.enabled) {
                list_out.add(list.get(i));
            }
        }

        return list_out;
    }

    public static List getInstalledApps(Context context) {
        List<ApplicationInfo> list
                = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> list_out = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ApplicationInfo entry = list.get(i);
            if (((entry.flags & ApplicationInfo.FLAG_INSTALLED) != 0)
                    && ((entry.flags & ApplicationInfo.FLAG_SYSTEM) == 0)) {
                list_out.add(list.get(i));
            }
        }

        return list_out;
    }

    public static Iterable<PermissionInfo> getPermissionsForPackage(PackageManager pm, String packageName) {
        ArrayList<PermissionInfo> retval = new ArrayList<>();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);

            if (packageInfo.requestedPermissions != null) {
                for (String permName : packageInfo.requestedPermissions) {
                    try {
                        retval.add(pm.getPermissionInfo(permName, PackageManager.GET_META_DATA));
                    } catch (PackageManager.NameNotFoundException e) {
                        // Not an official android permission... no big deal
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TAG", "That's odd package: " + packageName + " should be here but isn't");
        }
        return retval;
    }

    public static boolean setBluetoothEnabled(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    public static DeviceCfg getDeviceCfg(Context context) {
        try {
            String product = Build.PRODUCT;

            XmlPullParserFactory pullParserFactory;
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();

            InputStream in_s = context.getAssets().open(product + "_cfg.xml");
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in_s, null);

            DeviceCfg cfg = new DeviceCfg();
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String name = parser.getName();
                        if (Objects.equals(name, "product")) {
                            cfg.product = parser.nextText();
                        } else if (Objects.equals(name, "brand")) {
                            cfg.brand = parser.nextText();
                        } else if (Objects.equals(name, "model")) {
                            cfg.model = parser.nextText();
                        } else if (Objects.equals(name, "has_vibro_intensity")) {
                            cfg.hasVibroIntensety = !Objects.equals(parser.nextText(), "0");
                        } else if (Objects.equals(name, "vibro_intensity_path")) {
                            cfg.vibroIntensetyPath = parser.nextText();
                        } else if (Objects.equals(name, "vibro_intensity_min")) {
                            cfg.vibroIntensetyMin = Integer.parseInt(parser.nextText());
                        } else if (Objects.equals(name, "vibro_intensity_max")) {
                            cfg.vibroIntensetyMax = Integer.parseInt(parser.nextText());
                        } else if (Objects.equals(name, "vibro_intensity_default")) {
                            cfg.vibroIntensetyDefault = Integer.parseInt(parser.nextText());
                        } else if (Objects.equals(name, "brightness_path")) {
                            cfg.brightnessPath = parser.nextText();
                        } else if (Objects.equals(name, "brightness_min")) {
                            cfg.brightnessMin = Integer.parseInt(parser.nextText());
                        } else if (Objects.equals(name, "brightness_max")) {
                            cfg.brightnessMax = Integer.parseInt(parser.nextText());
                        } else if (Objects.equals(name, "touch_idc_path")) {
                            cfg.setTouchIdcPath(parser.nextText());
                        }
                        break;
                }
                eventType = parser.next();
            }
            return cfg;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return new DeviceCfg();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            //configuration not found, using default values
            Log.d(TAG, Build.PRODUCT + " configuration not found, using default values");
            return new DeviceCfg();
        } catch (IOException e) {
            e.printStackTrace();
            return new DeviceCfg();
        }
    }

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod2() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id", "-u"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = in.readLine();
            return line !=null && line.equals("0");
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static boolean isPackageInstalled(String packageName, Context context, int version) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return pi.versionCode >= version;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void setTiltToWake(boolean paramBoolean, Context paramContext) {
        SharedPreferences localSharedPreferences = paramContext.getSharedPreferences("home_preferences", 0);
        boolean bool = tiltToWakeEnabled(paramContext);
        if (bool == paramBoolean) {
            Log.w("TAG", "setTiltToWake to its old value: " + bool + " - ignoring!");
            return;
        }
        SharedPreferences.Editor localEditor = localSharedPreferences.edit();
        localEditor.putBoolean("tilt_to_wake", paramBoolean);
        localEditor.apply();
    }
}