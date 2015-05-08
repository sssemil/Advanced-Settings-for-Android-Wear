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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean tiltToWakeEnabled(Context paramContext) {
        return paramContext.getSharedPreferences("home_preferences", 0).getBoolean("tilt_to_wake", false);
    }

    /*
     * Get all installed application on mobile and return a list
     * @param   c   Context of application
     * @return  list of installed applications
     */
    public static List getInstalledApplication(Context c) {
        return c.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
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
            if(BuildConfig.DEBUG) {
                Log.e("TAG", "That's odd package: " + packageName + " should be here but isn't");
            }
        }
        return retval;
    }

    private void setTiltToWake(boolean paramBoolean, Context paramContext) {
        SharedPreferences localSharedPreferences = paramContext.getSharedPreferences("home_preferences", 0);
        boolean bool = tiltToWakeEnabled(paramContext);
        if (bool == paramBoolean) {
            if(BuildConfig.DEBUG) {
                Log.w("TAG", "setTiltToWake to its old value: " + bool + " - ignoring!");
            }
            return;
        }
        SharedPreferences.Editor localEditor = localSharedPreferences.edit();
        localEditor.putBoolean("tilt_to_wake", paramBoolean);
        localEditor.apply();
    }

    public static boolean setBluetoothEnabled(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }
}