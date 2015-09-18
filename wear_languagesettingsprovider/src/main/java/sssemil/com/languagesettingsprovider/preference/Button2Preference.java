package sssemil.com.languagesettingsprovider.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.io.IOException;


public class Button2Preference extends Preference {

    private static final String TAG = "Advanced Settings";
    private Context mContext;

    public Button2Preference(Context context, AttributeSet attrs) {
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
        try {
            if (!super.getActivity().equals("null")) {
                Runtime.getRuntime().exec(String.valueOf(super.getActivity()));
            }
        } catch (IOException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }

    }
}
