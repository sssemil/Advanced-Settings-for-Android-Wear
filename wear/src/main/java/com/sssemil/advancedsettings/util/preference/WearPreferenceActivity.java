package com.sssemil.advancedsettings.util.preference;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sssemil.advancedsettings.R;

import java.util.ArrayList;
import java.util.List;

public abstract class WearPreferenceActivity extends Activity implements WearableListView.ClickListener {

    public LayoutInflater inflater;

    WearableListView list;

    List<Preference> preferences = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);

        setContentView(R.layout.preference_list);
        list = (WearableListView) findViewById(android.R.id.list);
    }

    /**
     * Inflates the preferences from the given resource and displays them on this page.
     *
     * @param prefsResId The resource ID of the preferences xml file.
     */
    public void addPreferencesFromResource(@LayoutRes int prefsResId) {
        final View prefsRoot = inflater.inflate(prefsResId, null);

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use preference.PreferenceScreen as its root element");
        }

        addPreferencesFromPreferenceScreen((PreferenceScreen) prefsRoot);
    }

    public void addPreferencesFromView(View prefsRoot) {

        if (!(prefsRoot instanceof PreferenceScreen)) {
            throw new IllegalArgumentException("Preferences resource must use preference.PreferenceScreen as its root element");
        }

        addPreferencesFromPreferenceScreen((PreferenceScreen) prefsRoot);
    }

    public void addPreferencesFromPreferenceScreen(PreferenceScreen preferenceScreen) {
        final List<Preference> loadedPreferences = new ArrayList<>();
        for (int i = 0; i < preferenceScreen.getChildCount(); i++) {
            loadedPreferences.add(parsePreference(preferenceScreen.getChildAt(i)));
        }
        addPreferences(loadedPreferences);
    }

    public void addPreferences(List<Preference> newPreferences) {
        preferences = newPreferences;
        list.setAdapter(new SettingsAdapter());
        list.setClickListener(this);
    }

    public Preference parsePreference(View preferenceView) {
        if (preferenceView instanceof Preference) {
            return (Preference) preferenceView;
        }

        throw new IllegalArgumentException("Preferences layout resource may only contain Views extending preference.Preference");
    }

    public ListPreference parseListPreference(View preferenceView) {
        //if (preferenceView instanceof ListPreference) {
        return (ListPreference) preferenceView;
        //}

        //throw new IllegalArgumentException("Preferences layout resource may only contain Views extending preference.Preference");
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        final Preference clickedPreference = preferences.get(viewHolder.getPosition());
        clickedPreference.onPreferenceClick();
    }

    @Override
    public void onTopEmptyRegionClick() {
    }


    private class SettingsAdapter extends WearableListView.Adapter {
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final ListItemLayout itemView = new ListItemLayout(WearPreferenceActivity.this);
            return new WearableListView.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            final Preference preference = preferences.get(position);
            final ListItemLayout itemView = (ListItemLayout) holder.itemView;
            itemView.bindPreference(preference);
            itemView.onNonCenterPosition(false);
        }

        @Override
        public int getItemCount() {
            return preferences.size();
        }

        @Override
        public void onViewRecycled(WearableListView.ViewHolder holder) {
            final ListItemLayout itemView = (ListItemLayout) holder.itemView;
            itemView.releaseBinding();
        }
    }

}
