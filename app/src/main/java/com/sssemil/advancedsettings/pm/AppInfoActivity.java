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
package com.sssemil.advancedsettings.pm;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sssemil.advancedsettings.R;
import com.sssemil.advancedsettings.util.Utils;

import java.io.IOException;
import java.util.List;

public class AppInfoActivity extends Activity {

    private static final String TAG = "Advanced Settings";

    private String mPackageName;

    private TextView mAppNameView, mPackageNameView, mVersionView;
    private Button mForceStopButton, mUninstallButton;
    private ImageView mIcon;

    private PackageInfo mPackageInfo;
    private ApplicationInfo mApplicationInfo;

    private PackageManager mPackageManager;

    private Context mContext;

    private Thread mThread;

    private ActivityManager.RunningAppProcessInfo mProcess;

    private boolean mIsSystemApp = false;
    private String mEnable = "";
    private ActivityManager mActivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        mContext = getApplicationContext();

        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        mForceStopButton = (Button) findViewById(R.id.forse_stop_button);
        mUninstallButton = (Button) findViewById(R.id.uninstall_button);
        mForceStopButton.setEnabled(isAppRunning());

        mPackageName = getIntent().getStringExtra("packageName");

        mPackageManager = this.getPackageManager();

        mAppNameView = (TextView) findViewById(R.id.app_name);
        mPackageNameView = (TextView) findViewById(R.id.package_name);
        mVersionView = (TextView) findViewById(R.id.version);

        mIcon = (ImageView) findViewById(R.id.icon);

        mPackageNameView.setText(mPackageName);

        mThread = new Thread(new Task());
        mThread.start();

        TextView permissions_list = (TextView) findViewById(R.id.permisisions_list);

        try {
            mPackageInfo
                    = mPackageManager.getPackageInfo(mPackageName, 0);
            mApplicationInfo
                    = mPackageManager.getApplicationInfo(mPackageName, 0);
            if ((mApplicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                if (mApplicationInfo.enabled) {
                    mUninstallButton.setText(getString(R.string.disable));
                    mEnable = "disable";
                } else {
                    mUninstallButton.setText(getString(R.string.enable));
                    mEnable = "enable";
                }
                mIsSystemApp = true;
            }
            mIcon.setBackground(mPackageManager.getApplicationIcon(mApplicationInfo));
            mAppNameView.setText(mPackageManager.getApplicationLabel(mApplicationInfo));
            mVersionView.setText("version " + mPackageInfo.versionName);
            Iterable<PermissionInfo> permissions
                    = Utils.getPermissionsForPackage(mPackageManager, mPackageName);
            boolean did = false;
            for (PermissionInfo permission : permissions) {
                permissions_list.append(permission.name + "\n");
                did = true;
            }
            if (!did) {
                permissions_list.append(getString(R.string.no_perm));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
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

    public void onForceStopClick(View view) {
        //TODO: Do it the right way
        android.os.Process.killProcess(mProcess.pid);

        Log.d("PID", mProcess.pid + " name: " + mProcess.processName);

        if (isAppRunning()) {
            try {
                Runtime.getRuntime().exec("su -c killall " + mPackageName);
                Runtime.getRuntime().exec("su -c killall " + mPackageName);
                Runtime.getRuntime().exec("su -c killall " + mPackageName);
                Runtime.getRuntime().exec("su -c killall " + mPackageName);
                Runtime.getRuntime().exec("su -c killall " + mPackageName);
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            }
        }
    }

    public void onUninstallClick(View view) {
        if (!mIsSystemApp) {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + mPackageName));
            startActivity(intent);
        } else {
            try {
                Runtime.getRuntime().exec("su -c pm " + mEnable + " " + mPackageName);
            } catch (IOException e) {
                Log.d(TAG, "catch " + e.toString() + " hit in run", e);
            }
        }
    }

    public boolean isAppRunning() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (procInfos.get(i).processName.equals(mPackageName)) {
                mProcess = procInfos.get(i);
                return true;
            }
        }
        return false;
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
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

                if (!isPackageInstalled(mPackageName, mContext)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }

                final boolean isAppRunning = isAppRunning();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mForceStopButton.setEnabled(isAppRunning);
                    }
                });

                if (mIsSystemApp) {
                    try {
                        mApplicationInfo
                                = mPackageManager.getApplicationInfo(mPackageName, 0);

                        if (mApplicationInfo.enabled) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mUninstallButton.setText(getString(R.string.disable));
                                }
                            });
                            mEnable = "disable";
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mUninstallButton.setText(getString(R.string.enable));
                                }
                            });
                            mEnable = "enable";
                        }

                    } catch (PackageManager.NameNotFoundException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                }
            }
        }
    }
}
