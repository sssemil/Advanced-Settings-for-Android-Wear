package com.sssemil.advancedsettings;

import android.os.Bundle;
import android.view.View;

import com.sssemil.advancedsettings.util.Utils;
import com.sssemil.advancedsettings.util.preference.Preference;
import com.sssemil.advancedsettings.util.preference.PreferenceScreen;
import com.sssemil.advancedsettings.util.preference.WearPreferenceActivity;

import java.util.ArrayList;
import java.util.List;

public class SecuritySettingsActivity extends WearPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View prefsRoot = inflater.inflate(R.layout.activity_security_settings, null);

        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < ((PreferenceScreen) prefsRoot).getChildCount(); i++) {
            if ((parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)).getKey())
                    .equals("unpair_settings")) {
                if (Utils.isPackageInstalled("sssemil.com.screensavertimeoutplugin", this, 2)) {
                    loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
                }
            } else {
                loadedPreferences.add(parsePreference(((PreferenceScreen) prefsRoot).getChildAt(i)));
            }
        }
        addPreferences(loadedPreferences);
    }
}
