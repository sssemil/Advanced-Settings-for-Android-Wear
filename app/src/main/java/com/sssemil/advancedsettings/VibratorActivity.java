package com.sssemil.advancedsettings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.sssemil.advancedsettings.preference.WearPreferenceActivity;

import java.io.IOException;

public class VibratorActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_vibrator);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("vibration_intensity")) {
            try {
                ProcessBuilder pb
                        = new ProcessBuilder("su", "-c", "echo",
                        sharedPreferences.getString("vibration_intensity", null) + ">",
                        "/sys/class/timed_output/vibrator/driving_ms");
                pb.start();

                int ms = Integer.parseInt(sharedPreferences.getString("vibration_intensity", null));

                int amp = (ms - 20)*(100-80)/(200-20);

                ProcessBuilder pb2
                        = new ProcessBuilder("su", "-c", "echo",
                        amp + ">",
                        "/sys/class/timed_output/vibrator/amp");
                pb2.start();

                ProcessBuilder pb3
                        = new ProcessBuilder("su", "-c", "echo",
                        1000 + ">",
                        "/sys/class/timed_output/vibrator/enable");
                pb3.start();

                Log.d("Vibration", "amp: " + amp + " delay_ms: " + ms );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
