package com.sssemil.advancedsettings.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DeviceCfg {
    //Settings default values in case of unsupported device
    public String product = "lenok";
    public String brand = "lge";
    public String model = "G Watch R";
    public String vibroIntensetyPath = "/sys/class/timed_output/vibrator/amp";
    public String brightnessPath = "/sys/class/leds/lcd-backlight/brightness";
    public boolean hasVibroIntensety = true;
    public int vibroIntensetyMin = 10;
    public int vibroIntensetyDefault = 80;
    public int vibroIntensetyMax = 100;
    public int brightnessMin = 10;
    public int brightnessDefault = 120;
    public int brightnessMax = 254;
    private String touchIdcPath = "/system/usr/idc/sec_touchscreen.idc";

    public String getTouchIdcPath(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getString("idc_path", null) != null) {
            return sharedPreferences.getString("idc_path", null);
        } else {
            File idc = new File(touchIdcPath);
            if (!idc.exists()) {
                File dir = new File("/system/usr/idc/");
                try {
                    Runtime.getRuntime().exec("su -c chmod 644 /system/usr/idc/*").waitFor();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
                ArrayList<String> files = new ArrayList<>();
                try {
                    files = searchFiles(dir, "touch.wake =", files);
                    Log.i("FILES", files.toString());
                    touchIdcPath = "/system/usr/idc/" + files.get(0);
                    sharedPreferences.edit().putString("idc_path", "/system/usr/idc/" + files.get(0)).apply();
                    return "/system/usr/idc/" + files.get(0);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                sharedPreferences.edit().putString("idc_path", touchIdcPath).apply();
                return touchIdcPath;
            }
        }
        return null;
    }

    public void setTouchIdcPath(String touchIdcPath) {
        this.touchIdcPath = touchIdcPath;
    }

    private ArrayList<String> searchFiles(File file, String pattern, ArrayList<String> result)
            throws FileNotFoundException {

        if (!file.isDirectory()) {
            throw new IllegalArgumentException("file has to be a directory");
        }

        if (result == null) {
            result = new ArrayList<>();
        }

        File[] files = file.listFiles();

        if (files != null) {
            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    searchFiles(currentFile, pattern, result);
                } else {
                    Scanner scanner = new Scanner(currentFile);
                    if (scanner.findWithinHorizon(pattern, 0) != null) {
                        result.add(currentFile.getName());
                    }
                    scanner.close();
                }
            }
        }
        return result;
    }
}