package com.sssemil.advancedsettings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class OnBootReceiver extends BroadcastReceiver {
    public static final String TAG = "HEY!!!";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "started 1 :D");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "started 2 :D");
            context.startService(new Intent(context, MainService.class));
        }
    }
}