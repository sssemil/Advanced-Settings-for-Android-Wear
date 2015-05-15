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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sssemil.advancedsettings.BuildConfig;
import com.sssemil.advancedsettings.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class UninstallAppActivity extends Activity {

    private static final String TAG = "UninstallAppActivity";
    private Uri mPackageURI;

    private TextView mLabel, mVersion, mLog;
    private Button mInstall, mCancel;
    private ImageView mIcon;

    private boolean mSucceed = false;
    private ApplicationInfo mApplicationInfo;
    private String mPackageName;
    private PackageManager mPackageManager;
    private PackageInfo mPackageInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uninstall_app);

        final Intent intent = getIntent();
        mPackageURI = intent.getData();
        mPackageManager = this.getPackageManager();

        mLabel = (TextView) findViewById(R.id.label);
        mVersion = (TextView) findViewById(R.id.version);
        mLog = (TextView) findViewById(R.id.log);

        mInstall = (Button) findViewById(R.id.install);
        mCancel = (Button) findViewById(R.id.cancel);

        mIcon = (ImageView) findViewById(R.id.icon);

        try {

            if (mPackageURI == null) {
                System.err.println("No package URI in intent");
                mLog.append("No package URI in intent");
                mCancel.setText(getString(R.string.finish));
                mCancel.setEnabled(true);
            } else {
                mPackageName = mPackageURI.getEncodedSchemeSpecificPart();
                if (mPackageName == null) {
                    System.err.println("Invalid package name in URI: " + mPackageURI);
                    mLog.append("Invalid package name in URI: " + mPackageURI);
                    mCancel.setText(getString(R.string.finish));
                    mCancel.setEnabled(true);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, mPackageName);
                    }
                    mPackageInfo = mPackageManager.getPackageInfo(mPackageName, 0);
                    mApplicationInfo
                            = mPackageManager.getApplicationInfo(mPackageName, 0);
                    mIcon.setBackground(mPackageManager.getApplicationIcon(mApplicationInfo));
                    mLabel.setText(mPackageManager.getApplicationLabel(mApplicationInfo));
                    mVersion.setText(getString(R.string.ver) + " " + mPackageInfo.versionName);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public void onUninstallClick(View v) {
        if (mSucceed) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(mPackageName);
            startActivity(launchIntent);
        } else {
            mLog.append(getString(R.string.working));
            mInstall.setVisibility(View.INVISIBLE);
            mCancel.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessBuilder pb
                                = new ProcessBuilder("su", "-c", "pm", "uninstall", mPackageName);
                        pb.directory(new File("/"));
                        Process proc = pb.start();

                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(proc.getInputStream()));

                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(proc.getErrorStream()));

                        String log = "";
                        String s;
                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                            final String finalS = s;
                            log += s + "\n";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLog.append(finalS + "\n");
                                }
                            });
                        }

                        while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                            final String finalS = s;
                            log += s + "\n";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLog.append(finalS + "\n");
                                }
                            });
                        }
                        if (log.contains("Failure")) {
                            mSucceed = false;
                        } else if (log.contains("Success")) {
                            mSucceed = true;
                        }
                        if (!mSucceed) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLog.append(getString(R.string.failed));
                                    mCancel.setText(getString(R.string.finish));
                                    mCancel.setEnabled(true);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLog.append(getString(R.string.done));
                                    mCancel.setText(getString(R.string.finish));
                                    mCancel.setEnabled(true);
                                }
                            });
                        }
                    } catch (IOException e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    public void onCancelClick(View v) {
        finish();
    }

}
