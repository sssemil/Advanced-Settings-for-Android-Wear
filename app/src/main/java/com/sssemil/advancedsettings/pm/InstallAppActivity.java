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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sssemil.advancedsettings.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class InstallAppActivity extends Activity {

    private Uri mPackageURI;

    private TextView mLabel, mVersion, mLog;
    private Button mInstall, mCancel;
    private ImageView mIcon;

    private boolean mSucceed = false;
    private PackageInfo mPackageInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_app);

        final Intent intent = getIntent();

        mPackageURI = intent.getData();

        mLabel = (TextView) findViewById(R.id.label);
        mVersion = (TextView) findViewById(R.id.version);
        mLog = (TextView) findViewById(R.id.log);

        mInstall = (Button) findViewById(R.id.uninstall);
        mCancel = (Button) findViewById(R.id.cancel);

        mIcon = (ImageView) findViewById(R.id.icon);

        if (!new File(mPackageURI.getPath()).exists()) {
            System.err.println("Error: File Not Found: " + mPackageURI.getPath());
            mLog.append("Error: File Not Found: " + mPackageURI.getPath() + "\n" + getString(R.string.failed));
            mCancel.setText(getString(R.string.finish));
            mCancel.setEnabled(true);
        } else {
            PackageManager pm = getPackageManager();
            mPackageInfo = pm.getPackageArchiveInfo(mPackageURI.getPath(), 0);

            mPackageInfo.applicationInfo.sourceDir = mPackageURI.getPath();
            mPackageInfo.applicationInfo.publicSourceDir = mPackageURI.getPath();

            mLabel.setText(mPackageInfo.applicationInfo.loadLabel(pm));
            mIcon.setBackground(mPackageInfo.applicationInfo.loadIcon(pm));
            mVersion.setText(getString(R.string.version) + mPackageInfo.versionName);
        }
    }

    public void onInstallClick(View v) {
        if (mSucceed) {
            Intent launchIntent
                    = getPackageManager().getLaunchIntentForPackage(mPackageInfo.packageName);
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
                                = new ProcessBuilder("su", "-c", "pm", "install", mPackageURI.getPath());
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
                                    mInstall.setText(getString(R.string.open));
                                    mInstall.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void onCancelClick(View v) {
        finish();
    }

}
