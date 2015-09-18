package com.sssemil.advancedsettings;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    grandPermissions(MainActivity.this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-52564878-2");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
    }

    public void onDonateClick(View view) {
        Intent intent = new Intent(this, DonateActivity.class);
        this.startActivity(intent);
    }

    public void onRateItClick(View view) {
        Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.sssemil.advancedsettings");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onHideClick(View view) {
        PackageManager p = getPackageManager();
        ComponentName componentName
                = new ComponentName(this, MainActivity.class);
        p.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    //Useful stuff for feature stuff :P
    public static void grandPermissions(Context context) throws IOException {
        Runtime rt = Runtime.getRuntime();
        String package_name = context.getPackageName();
        String command = "su -c \"pm grant " + package_name + " android.permission.CHANGE_CONFIGURATION\"";
        Log.i("COMMAND", command);
        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));

        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
}
