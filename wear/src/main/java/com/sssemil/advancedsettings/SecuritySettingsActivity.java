package com.sssemil.advancedsettings;

import android.os.Bundle;

import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

public class SecuritySettingsActivity extends WearPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_security_settings);
    }
}
