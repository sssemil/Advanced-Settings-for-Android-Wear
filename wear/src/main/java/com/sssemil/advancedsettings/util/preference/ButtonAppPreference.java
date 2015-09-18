package com.sssemil.advancedsettings.util.preference;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;


public class ButtonAppPreference extends Preference {

    private static final String TAG = "Advanced Settings";
    private Context mContext;

    public ButtonAppPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    public int getIcon() {
        // Delegate to super if no specific icons are set
        return super.getIcon();
    }

    @Override
    public void onPreferenceClick() {
            if (!super.getActivity().equals("null")) {
                Intent intent = new Intent();
                intent
                        .setAction(Intent.ACTION_VIEW)
                        .setClassName(String.valueOf(super.getPackageName()),
                                String.valueOf(super.getActivity()));
                mContext.startActivity(intent);
            }

    }
}
