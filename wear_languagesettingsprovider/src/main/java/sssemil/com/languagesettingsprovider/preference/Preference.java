package sssemil.com.languagesettingsprovider.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import sssemil.com.languagesettingsprovider.R;

public abstract class Preference extends View {

    protected final String key;
    @DrawableRes
    protected final int icon;
    protected final boolean icon_vis;
    protected final String title;
    protected final String summary;
    protected final String activity;
    protected final String package_name;

    protected final String defaultValue;

    public Preference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Preference, 0, 0);
        try {
            key = array.getString(R.styleable.Preference_pref_key);
            icon = array.getResourceId(R.styleable.Preference_pref_icon, 0);
            icon_vis = array.getBoolean(R.styleable.Preference_pref_icon_vis, true);
            title = array.getString(R.styleable.Preference_pref_title);
            summary = array.getString(R.styleable.Preference_pref_summary);
            activity = array.getString(R.styleable.Preference_pref_activity);
            package_name = array.getString(R.styleable.Preference_pref_package_name);
            defaultValue = array.getString(R.styleable.Preference_pref_defaultValue);

            checkRequiredAttributes();
        } finally {
            array.recycle();
        }
    }

    private void checkRequiredAttributes() {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Missing preference key from preference item");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        throw new IllegalStateException("Preferences should not be attached to a UI. They are for use with WearPrefrenceActivity only.");
    }

    /**
     * Returns the key for this preference.
     */
    @NonNull
    public String getKey() {
        return key;
    }

    /**
     * Returns a resource ID for the icon to display with this preference.
     * Subclasses may override this to display an icon based on the value of the preference.
     */
    @DrawableRes
    public int getIcon() {
        return icon;
    }

    public boolean getIconVis() {
        return icon_vis;
    }

    public CharSequence getActivity() {
        return activity;
    }

    public CharSequence getPackageName() {
        return package_name;
    }

    /**
     * Returns the title text to display to the user for this preference.
     * <p/>
     * By default, this will return the string defined in the xml attribute for
     * this preference (app:title).
     * <p/>
     * Subclasses will typically not override this method but may do so in some cases.
     */
    public CharSequence getTitle() {
        return title;
    }

    /**
     * Returns the summary text to display to the user.
     * Subclasses may override this to display the preference value to the user.
     */
    public CharSequence getSummary() {
        return summary;
    }

    /**
     * Called when the preference item is clicked by the user.
     */
    public abstract void onPreferenceClick();

}
