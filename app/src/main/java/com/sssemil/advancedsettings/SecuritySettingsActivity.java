package com.sssemil.advancedsettings;

import android.os.Bundle;

import com.sssemil.advancedsettings.preference.WearPreferenceActivity;

public class SecuritySettingsActivity extends WearPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_security_settings);
    }
}
