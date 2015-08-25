package com.sssemil.advancedsettings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.TextView;

import com.sssemil.advancedsettings.util.CircularSeekBar;
import com.sssemil.advancedsettings.util.DeviceCfg;

public class BrightnessActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);

        final TextView num = (TextView) findViewById(R.id.num);
        CircularSeekBar circularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekbar);

        final int maxBrightness = (new DeviceCfg()).brightnessMax;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int currentBrightness = Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, 0);

        circularSeekBar.setMaxProgress(maxBrightness);
        circularSeekBar.setProgress(currentBrightness);

        num.setText(currentBrightness + "/" + maxBrightness);

        circularSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress) {
                num.setText(view.getProgress() + "/" + maxBrightness);
                sharedPreferences.edit().putString("brightness_settings",
                        String.valueOf(view.getProgress())).apply();
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        view.getProgress());
            }
        });
    }
}
