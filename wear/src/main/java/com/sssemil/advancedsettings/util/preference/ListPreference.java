package com.sssemil.advancedsettings.util.preference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import com.sssemil.advancedsettings.R;


public class ListPreference extends Preference {

    public CharSequence[] entries, entryValues;
    public boolean useEntryAsSummary;
    public AttributeSet attrs;

    public ListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.attrs = attrs;

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ListPreference, 0, 0);
        try {
            entries = array.getTextArray(R.styleable.ListPreference_pref_entries);
            entryValues = array.getTextArray(R.styleable.ListPreference_pref_entryValues);
            useEntryAsSummary = array.getBoolean(R.styleable.ListPreference_pref_entryAsSummary, true);
            //checkRequiredAttributes();
        } finally {
            array.recycle();
        }
    }

    public ListPreference(Context context, AttributeSet attrs, CharSequence[] entries, CharSequence[] entryValues, String defaultValue, boolean useEntryAsSummary) {
        super(context, attrs);
        this.attrs = attrs;

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ListPreference, 0, 0);
        try {
            this.entries = entries;
            this.entryValues = entryValues;
            this.useEntryAsSummary = useEntryAsSummary;
            this.defaultValue = defaultValue;
            checkRequiredAttributes();
        } finally {
            array.recycle();
        }
    }

    private void checkRequiredAttributes() {
        if (entries == null || entryValues == null) {
            throw new IllegalArgumentException("ListPreference requires 'entries' and 'entryValues' attributes");
        }
    }

    private String getCurrentValue() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return preferences.getString(key, defaultValue);
    }

    private int getEntryPositionFor(CharSequence value) {
        for (int i = 0; i < entries.length; i++) {
            if (entryValues[i].toString().equals(value)) {
                return i;
            }
        }

        return -1;
    }

    private CharSequence getCurrentEntry() {
        final String currentValue = getCurrentValue();
        final int currentValuePos = getEntryPositionFor(currentValue);

        if (currentValuePos == -1) {
            return currentValue;
        } else {
            return entries[currentValuePos];
        }
    }

    @Override
    public CharSequence getSummary() {
        if (useEntryAsSummary) {
            return getCurrentEntry();
        } else {
            return super.getSummary();
        }
    }

    @Override
    public void onPreferenceClick() {
        final Intent chooseEntryIntent = ListChooserActivity.createIntent(
                getContext(),
                key,
                icon,
                entries,
                entryValues,
                getEntryPositionFor(getCurrentValue())
        );
        getContext().startActivity(chooseEntryIntent);
    }

}
