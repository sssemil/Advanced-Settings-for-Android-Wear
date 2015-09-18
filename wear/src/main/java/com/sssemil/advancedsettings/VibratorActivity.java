package com.sssemil.advancedsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.io.IOException;

public class VibratorActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Advanced Settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_vibrator);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("vibration_intensity")) {
            try {
                int amp = Integer.parseInt(
                        sharedPreferences.getString("vibration_intensity", null));
                ProcessBuilder pb
                        = new ProcessBuilder("su", "-c", "echo",
                        amp + ">",
                        Utils.getDeviceCfg(VibratorActivity.this).vibroIntensetyPath);
                pb.start().waitFor();

                Vibrator v = (Vibrator) VibratorActivity.this.getApplicationContext()
                        .getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            } catch (InterruptedException | IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            }
        }
    }
}
