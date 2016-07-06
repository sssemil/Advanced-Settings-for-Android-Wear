package com.sssemil.advancedsettings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MainActivity extends Activity {

    public static GoogleAnalytics analytics;
    public static Tracker tracker;
    public Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        analytics = GoogleAnalytics.getInstance(this);
        analytics.setLocalDispatchPeriod(1800);

        tracker = analytics.newTracker("UA-52564878-2");
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);

        if(!isWearAppInstalled()){
            (findViewById(R.id.hide)).setClickable(false);
            (findViewById(R.id.donate)).setClickable(false);
            (findViewById(R.id.rate)).setClickable(false);

            ((TextView) findViewById(R.id.small_info)).setText(R.string.no_app);
            ((TextView) findViewById(R.id.small_info)).setTextColor(Color.RED);
        }
    }

    public boolean isWearAppInstalled() {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo("com.google.android.wearable.app", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
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
}
