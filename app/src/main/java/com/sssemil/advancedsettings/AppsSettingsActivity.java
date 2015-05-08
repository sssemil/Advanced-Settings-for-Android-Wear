/*
 * Copyright (c) 2015 Emil Suleymanov <suleymanovemil8@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.sssemil.advancedsettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class AppsSettingsActivity extends Activity
        implements WearableListView.ClickListener {

    public List mListAppInfo;
    private Context mContext;
    private WearableListView mListView;
    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_settings);

        mContext = getApplicationContext();

        // Get the list component from the layout of the activity
        mListView =
                (WearableListView) findViewById(R.id.wearable_list);
        mListAppInfo = Utils.getInstalledApplication(this);
        // Assign an adapter to the list
        mListView.setAdapter(new Adapter(this, mListAppInfo, getPackageManager()));

        // Set a click listener
        mListView.setClickListener(this);

        mThread = new Thread(new Task());
        mThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mThread.isAlive()) {
            mThread.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        ApplicationInfo appInfo = (ApplicationInfo) mListAppInfo.get(v.getPosition());
        if(BuildConfig.DEBUG) {
            Log.i("TAG", appInfo.packageName);
        }

        Intent myIntent = new Intent(mContext, AppInfoActivity.class);
        myIntent.putExtra("packageName", appInfo.packageName);
        startActivity(myIntent);
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    private static final class Adapter extends WearableListView.Adapter {
        private final PackageManager mPm;
        private final LayoutInflater mInflater;
        private List mListAppInfo;

        // Provide a suitable constructor (depends on the kind of dataset)
        public Adapter(Context context, List list, PackageManager pm) {
            mInflater = LayoutInflater.from(context);
            mListAppInfo = list;
            mPm = pm;
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
            ApplicationInfo entry = (ApplicationInfo) mListAppInfo.get(position);
            ivAppIcon.setImageDrawable(entry.loadIcon(mPm));
            tvAppName.setText(entry.loadLabel(mPm));
            tvPkgSize.setText("");
            // replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mListAppInfo.size();
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

    class Task implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                List listAppInfo = Utils.getInstalledApplication(mContext);
                for (int n = 0; n < mListAppInfo.size(); n++) {
                    if (!((ApplicationInfo) mListAppInfo.get(n)).packageName.equals(
                            ((ApplicationInfo) listAppInfo.get(n)).packageName)) {
                        mListAppInfo = listAppInfo;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mListView.setAdapter(
                                        new Adapter(mContext, mListAppInfo, getPackageManager()));
                            }
                        });
                        break;
                    }
                }
            }
        }
    }
}
