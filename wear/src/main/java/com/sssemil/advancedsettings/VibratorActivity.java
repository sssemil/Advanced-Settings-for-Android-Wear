package com.sssemil.advancedsettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.sssemil.advancedsettings.util.DeviceCfg;
import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.ListPreference;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VibratorActivity extends WearPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Advanced Settings";
    private DeviceCfg mCfg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCfg = Utils.getDeviceCfg(this);

        final View prefsRoot = inflater.inflate(R.layout.activity_vibrator, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use" +
                    " preference.PreferenceScreen as its root element");
        }

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            switch ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())) {
                case "vibration_intensity":
                    ListPreference pref = (ListPreference) parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i));
                    ArrayList<CharSequence> entries = new ArrayList<>();
                    for (int j = mCfg.vibroIntensetyMin; j <= mCfg.vibroIntensetyMax; j += 5) {
                        entries.add(String.valueOf(j));
                    }
                    CharSequence[] arr = new CharSequence[entries.size()];
                    entries.toArray(arr);
                    pref.entries = arr;
                    pref.entryValues = pref.entries;
                    pref.defaultValue = String.valueOf(mCfg.vibroIntensetyDefault);
                    loadedPreferences.add(pref);
                    break;
                default:
                    loadedPreferences.add(parsePreference(((PreferenceScreen)
                            prefsRoot).getChildAt(i)));
                    break;
            }
        }
        addPreferences(loadedPreferences);

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
                        mCfg.vibroIntensetyPath);
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
