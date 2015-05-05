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
