package com.sssemil.advancedsettings;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import java.util.regex.Pattern;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView version = (TextView) findViewById(R.id.app_version);
        TextView link1 = (TextView) findViewById(R.id.textView3);
        TextView link2 = (TextView) findViewById(R.id.textView4);

        Pattern pattern = Pattern.compile("https://github.com/sssemil/wear_AdvancedSettings");
        Linkify.addLinks(link1, pattern, "");

        pattern = Pattern.compile("https://github.com/denley/WearPreferenceActivity");
        Linkify.addLinks(link2, pattern, "");

        String versionS = "-.-.-";
        try {
            versionS = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version.setText(getString(R.string.ver) + " " + versionS);
    }
}
