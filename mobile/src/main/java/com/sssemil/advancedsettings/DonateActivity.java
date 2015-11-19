package com.sssemil.advancedsettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class DonateActivity extends Activity implements Button.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View promptsView = layoutInflater.inflate(R.layout.donations, null);

        Button usd = (Button) promptsView.findViewById(R.id.usd);

        usd.setOnClickListener(this);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle(getString(R.string.app_name));
        adb.setView(promptsView);
        adb.show();
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        button.getId();
        Uri uri;
        Intent intent;
        switch (button.getId()) {
            case (R.id.usd):
                uri = Uri.parse("https://play.google.com/store/apps/details?id=sssemil.com.donation");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case (R.id.btc):
                uri = Uri.parse("http://sssemil.github.io/Donate/btc.html");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
