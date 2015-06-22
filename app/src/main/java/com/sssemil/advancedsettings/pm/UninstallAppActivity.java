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

import com.sssemil.advancedsettings.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UninstallAppActivity extends Activity {

    private static final String TAG = "UninstallAppActivity";
    private Uri mPackageURI;

    private TextView mLabel, mVersion, mLog, mWarning;
    private Button mUninstall, mCancel;
    private ImageView mIcon;

    private boolean mSucceed = false;
    private ApplicationInfo mApplicationInfo;
    private String mPackageName;
    private PackageManager mPackageManager;
    private PackageInfo mPackageInfo;
    private boolean mIsSystemApp = false;


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
        mWarning = (TextView) findViewById(R.id.warning);

        mUninstall = (Button) findViewById(R.id.install);
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
                mPackageInfo = mPackageManager.getPackageInfo(mPackageName, 0);
                mApplicationInfo = mPackageManager.getApplicationInfo(mPackageName, 0);

                if ((mApplicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    mIsSystemApp = true;
                    mWarning.setText(R.string.system_uninstall_warning);
                } else {
                    mIsSystemApp = false;
                    mWarning.setText(R.string.normal_uninstall_warning);
                }

                if (mPackageName == null) {
                    System.err.println("Invalid package name in URI: " + mPackageURI);
                    mLog.append("Invalid package name in URI: " + mPackageURI);
                    mCancel.setText(getString(R.string.finish));
                    mCancel.setEnabled(true);
                } else {
                    mIcon.setBackground(mPackageManager.getApplicationIcon(mApplicationInfo));
                    mLabel.setText(mPackageManager.getApplicationLabel(mApplicationInfo));
                    mVersion.setText(getString(R.string.ver) + " " + mPackageInfo.versionName);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "catch " + e.toString() + " hit in run", e);
        }
    }

    public void onUninstallClick(View v) {
        if (mSucceed) {
            finish();
        } else {
            mSucceed = true;
            mLog.append(getString(R.string.working));
            mUninstall.setVisibility(View.INVISIBLE);
            mCancel.setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!mIsSystemApp) {
                            ProcessBuilder pb = new ProcessBuilder("su", "-c", "pm", "uninstall", mPackageName);
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
                        } else {
                            ProcessBuilder pb0 = new ProcessBuilder(
                                    "su", "-c", "mount", "-o", "remount,rw", "/system");
                            Process proc0 = pb0.start();

                            BufferedReader stdInput0 = new BufferedReader(new
                                    InputStreamReader(proc0.getInputStream()));

                            BufferedReader stdError0 = new BufferedReader(new
                                    InputStreamReader(proc0.getErrorStream()));

                            String s0;
                            while ((s0 = stdInput0.readLine()) != null) {
                                System.out.println(s0);
                            }

                            while ((s0 = stdError0.readLine()) != null) {
                                System.out.println(s0);
                            }

                            ProcessBuilder pb = new ProcessBuilder(
                                    "su", "-c", "rm", "-rf", mApplicationInfo.sourceDir);
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
                                mSucceed = false;
                            }
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
                                    mLog.append(getString(R.string.done) + "\n");
                                    mLog.append(getString(R.string.wait_till_sys));
                                    mCancel.setText(getString(R.string.finish));
                                    mCancel.setEnabled(true);
                                }
                            });
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "catch " + e.toString() + " hit in run", e);
                    }
                }
            }).start();
        }
    }

    public void onCancelClick(View v) {
        finish();
    }

}
