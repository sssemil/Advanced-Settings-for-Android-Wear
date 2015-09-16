package com.sssemil.advancedsettings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sssemil.advancedsettings.util.DeviceCfg;
import com.sssemil.advancedsettings.util.Utils;

public class ScreenSaverBrightnessActivity extends Activity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int mCurrentBrightness;
    private TextView mNum;
    private int maxBrightness;
    private int minBrightness;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brightness);

        mNum = (TextView) findViewById(R.id.num);

        final DeviceCfg deviceCfg = Utils.getDeviceCfg(this);

        maxBrightness = deviceCfg.brightnessMax;
        minBrightness = deviceCfg.brightnessMin;

        final SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mCurrentBrightness = Integer.parseInt(
                sharedPreferences.getString(
                        "screen_saver_brightness_settings",
                        String.valueOf(deviceCfg.brightnessDefault)));

        mNum.setText(mCurrentBrightness + "/" + maxBrightness);

        final ImageButton up = (ImageButton) findViewById(R.id.up);
        final ImageButton down = (ImageButton) findViewById(R.id.down);

        up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    up.setPressed(true);
                    v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (up.isPressed()) {
                                    if (mCurrentBrightness < maxBrightness) {
                                        set(mCurrentBrightness + 1, sharedPreferences);
                                        mCurrentBrightness = Integer.parseInt(
                                                sharedPreferences.getString(
                                                        "screen_saver_brightness_settings",
                                                        String.valueOf(deviceCfg.brightnessDefault)));
                                        sleep(50);
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    t.start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    up.setPressed(false);
                    v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                }
                return true;
            }
        });

        down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    down.setPressed(true);
                    v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (down.isPressed()) {
                                    if (mCurrentBrightness > minBrightness) {
                                        set(mCurrentBrightness - 1, sharedPreferences);
                                        mCurrentBrightness = Integer.parseInt(
                                                sharedPreferences.getString(
                                                        "screen_saver_brightness_settings",
                                                        String.valueOf(deviceCfg.brightnessDefault)));
                                        sleep(50);
                                    }
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    t.start();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    down.setPressed(false);
                    v.playSoundEffect(android.view.SoundEffectConstants.CLICK);
                }
                return true;
            }
        });
    }

    private void set(int value, SharedPreferences sharedPreferences) {
        sharedPreferences.edit().putString("screen_saver_brightness_settings",
                String.valueOf(value)).apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("screen_saver_brightness_settings")) {
            mNum.setText(mCurrentBrightness + "/" + maxBrightness);
        }
    }
}