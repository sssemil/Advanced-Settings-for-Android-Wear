package com.sssemil.advancedsettings.util;

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
}