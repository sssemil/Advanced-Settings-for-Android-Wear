package com.sssemil.advancedsettings.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class DeviceCfg {

    public static final String GOVERNOR_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOVERNORS_LIST_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    public static final String FREQUENCIES_LIST_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String MIN_FREQUENCY_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String MAX_FREQUENCY_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";

    //Settings default values in case of unsupported device
    public String product = "unknown";
    public String brand = "unknown";
    public String model = "unknown";

    public String vibroIntensetyPath = "/sys/class/timed_output/vibrator/amp";
    public String brightnessPath = "/sys/class/leds/lcd-backlight/brightness";

    private String touchIdcPath = "/system/usr/idc/sec_touchscreen.idc";

    public boolean hasVibroIntensety = true;

    public int vibroIntensetyMin = 10;
    public int vibroIntensetyDefault = 80;
    public int vibroIntensetyMax = 100;
    public int brightnessMin = 10;
    public int brightnessDefault = 120;
    public int brightnessMax = 254;

    public static ArrayList<String> getAvailableFrequencies() throws IOException {
        return getValuesList(FREQUENCIES_LIST_PATH);
    }

    public static String getMinFrequency() throws IOException, InterruptedException {
        return getValue(MIN_FREQUENCY_PATH);
    }

    public static boolean setMinFrequency(String value) throws IOException, InterruptedException {
        return setValue(value, MIN_FREQUENCY_PATH);
    }

    public static String getMaxFrequency() throws IOException, InterruptedException {
        return getValue(MAX_FREQUENCY_PATH);
    }

    public static boolean setMaxFrequency(String value) throws IOException, InterruptedException {
        return setValue(value, MAX_FREQUENCY_PATH);
    }

    public static ArrayList<String> getAvailableGovernors() throws IOException {
        return getValuesList(GOVERNORS_LIST_PATH);
    }

    public static String getCurrentGovernor() throws IOException, InterruptedException {
        return getValue(GOVERNOR_PATH);
    }

    public static boolean setCurrentGovernor(String value) throws IOException, InterruptedException {
        return setValue(value, GOVERNOR_PATH);
    }

    public static boolean setValue(String value, String path) throws IOException, InterruptedException {
        if (value == null) {
            return false;
        }

        ProcessBuilder pb
                = new ProcessBuilder("su", "-c", "echo",
                value, ">",
                path);
        pb.start().waitFor();

        return getValue(path).equals(value);
    }

    public static String getValue(String path) throws IOException, InterruptedException {
        StringBuilder result = new StringBuilder();

        File file = new File(path);

        if (!file.canRead()) {
            //su -c chmod a+r
            ProcessBuilder pb
                    = new ProcessBuilder("su", "-c", "chmod", "a+r", path);
            pb.start().waitFor();
        }

        try (Scanner in = new Scanner(file)) {
            String line;
            while (in.hasNextLine()) {
                line = in.nextLine();

                if (line != null && line.length() > 0) {
                    result.append(line);
                    if (in.hasNextLine()) {
                        result.append("\n");
                    }
                }
            }
        }

        return result.toString();
    }

    public static ArrayList<String> getValuesList(String path) throws IOException {
        ArrayList<String> result = new ArrayList<>();

        try (Scanner in = new Scanner(new File(path))) {
            String line = null;
            while (in.hasNext()) {
                line = in.nextLine();

                if (line != null && line.length() > 0) {
                    String[] spl = line.split(" ");
                    Collections.addAll(result, spl);
                }
            }
        }

        return result;
    }

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