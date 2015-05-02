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

    private Thread mChanger;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "STARTED!!!");
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        mDisplayManager.registerDisplayListener(this, null);

        mChanger = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1);
                    ProcessBuilder pb
                            = new ProcessBuilder("su", "-c", "echo",
                            Settings.System.getInt(getContentResolver(),
                                    Settings.System.SCREEN_BRIGHTNESS, 0) + ">",
                            "/sys/class/leds/lcd-backlight/brightness");
                    pb.start();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDisplayAdded(int displayId) {

    }

    @Override
    public void onDisplayRemoved(int displayId) {

    }

    @Override
    public void onDisplayChanged(int displayId) {
        switch (mDisplayManager.getDisplay(displayId).getState()) {
            case Display.STATE_DOZE_SUSPEND:
            case Display.STATE_DOZE:
                if(!mChanger.isAlive()){
                    mChanger.start();
                }
                break;
            default:
                break;
        }
    }
}
