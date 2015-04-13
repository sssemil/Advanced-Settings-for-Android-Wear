package preference;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;


public class ButtonPreference extends Preference {

    private Context mContext;

    public ButtonPreference(Context context, AttributeSet attrs) {
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
                mContext.startActivity(
                        new Intent(mContext,
                                Class.forName(String.valueOf(super.getActivity()))));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
