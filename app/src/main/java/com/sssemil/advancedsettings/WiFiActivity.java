package com.sssemil.advancedsettings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

public class WiFiActivity extends Activity implements WearableListView.ClickListener {

    public Switch mEnable;
    public TextView mState;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            switchState(state);
        }
    };
    public List mListWiFi;
    private WifiManager mWifiManager;
    private String mWiFis[];
    private Context mContext;
    private WearableListView mListView;
    private BroadcastReceiver mWiFiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();
            mListView.setAdapter(new Adapter(mContext, wifiScanList, getPackageManager()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mWifiManager.startScan();

        mContext = getApplicationContext();

        // Get the list component from the layout of the activity
        mListView = (WearableListView) findViewById(R.id.list);
        // Assign an adapter to the list
        //mListView.setAdapter(new Adapter(this, mListWiFi, getPackageManager()));

        // Set a click listener
        mListView.setClickListener(this);

        registerReceiver(mReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(mWiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        mEnable = (Switch) findViewById(R.id.enable);
        mState = (TextView) findViewById(R.id.state);

        mEnable.setChecked(mWifiManager.isWifiEnabled());
        switchState(mWifiManager.getWifiState());
    }

    private void cleanUp() {
        try {
            unregisterReceiver(mReceiver);
            unregisterReceiver(mWiFiReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    protected void onPause() {
        cleanUp();
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(mReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(mWiFiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public void onEnableClicked(View view) {
        mWifiManager.setWifiEnabled(((Switch) view).isChecked());
    }

    public void switchState(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                mState.setText(getString(R.string.off));
                mEnable.setEnabled(true);
                mEnable.setChecked(false);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                mState.setText(getString(R.string.turning_off));
                mEnable.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                mState.setText(getString(R.string.on));
                mEnable.setEnabled(true);
                mEnable.setChecked(true);
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                mState.setText(getString(R.string.turning_on));
                mEnable.setEnabled(false);
                break;
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    private static final class Adapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;
        private List mList;

        // Provide a suitable constructor (depends on the kind of dataset)
        public Adapter(Context context, List list, PackageManager pm) {
            mInflater = LayoutInflater.from(context);
            mList = list;
        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items
            return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {
            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            ImageView ivAppIcon = itemHolder.ivAppIcon;
            TextView tvAppName = itemHolder.tvAppName;
            TextView tvPkgSize = itemHolder.tvPkgSize;
            // replace text contents
            tvAppName.setText(((ScanResult) mList.get(position)).SSID);
            tvPkgSize.setText(((ScanResult) mList.get(position)).capabilities);
            // replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mList.size();
        }

        // Provide a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private ImageView ivAppIcon;
            private TextView tvAppName, tvPkgSize;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                ivAppIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
                tvAppName = (TextView) itemView.findViewById(R.id.tvName);
                tvPkgSize = (TextView) itemView.findViewById(R.id.tvSize);
            }
        }
    }
}
