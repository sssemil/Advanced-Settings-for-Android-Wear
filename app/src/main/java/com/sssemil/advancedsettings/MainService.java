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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;

import java.io.IOException;

public class MainService extends Service implements DisplayManager.DisplayListener {
    private static final String TAG = "MainService";
    private DisplayManager mDisplayManager;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "STARTED!!!");
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);
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
                    int brightness = Settings.System.getInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS, 0) - 20;
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
