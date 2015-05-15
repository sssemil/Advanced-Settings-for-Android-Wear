package com.sssemil.advancedsettings.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sssemil.advancedsettings.BuildConfig;

public class BluetoothReceiver extends BroadcastReceiver {
    public BluetoothReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            if (BuildConfig.DEBUG) {
                Log.i("BLUETOOTH", String.valueOf(state));
            }
        }
    }
}
