package com.sssemil.advancedsettings;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import com.sssemil.advancedsettings.util.Utils;

public class BluetoothActivity extends Activity {

    public Switch mEnable;
    public CheckBox mVisible;
    public TextView mState;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switchState(state);
            }
        }
    };
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mEnable = (Switch) findViewById(R.id.enable);
        mVisible = (CheckBox) findViewById(R.id.visible);

        mState = (TextView) findViewById(R.id.state);

        mEnable.setChecked(mBluetoothAdapter.isEnabled());
        switchState(mBluetoothAdapter.getState());

        mVisible.setChecked(mBluetoothAdapter.isDiscovering());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public void onEnableClicked(View view) {
        Utils.setBluetoothEnabled(((Switch) view).isChecked());
    }

    public void onMoreClick(View view) {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        this.startActivity(intent);
    }

    public void onVisibilityClick(View view) {
        if (((CheckBox) view).isChecked()) {
            Intent discoverableIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, 1);
        } else {
            mVisible.setChecked((mBluetoothAdapter.getScanMode()
                    == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("onActivityResult",
                String.valueOf((mBluetoothAdapter.getScanMode()
                        == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)));

        mVisible.setChecked((mBluetoothAdapter.getScanMode()
                == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE));
    }

    public void switchState(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                mState.setText(getString(R.string.off));
                mEnable.setEnabled(true);
                mEnable.setChecked(false);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mState.setText(getString(R.string.turning_off));
                mEnable.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                mState.setText(getString(R.string.on));
                mEnable.setEnabled(true);
                mEnable.setChecked(true);
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                mState.setText(getString(R.string.turning_on));
                mEnable.setEnabled(false);
                break;
        }
    }
}
