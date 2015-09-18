package com.sssemil.advancedsettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class DonateActivity extends Activity implements Button.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View promptsView = layoutInflater.inflate(R.layout.donations, null);

        Button regular = (Button) promptsView.findViewById(R.id.regular);
        Button great = (Button) promptsView.findViewById(R.id.great);
        Button giant = (Button) promptsView.findViewById(R.id.giant);

        regular.setOnClickListener(this);
        great.setOnClickListener(this);
        giant.setOnClickListener(this);

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
            case (R.id.regular):
                uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6J9K4Y42PT3ZE");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case (R.id.great):
                uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=7C7EL6ENPJ2NU");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case (R.id.giant):
                uri = Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=DU9MXS8HEQE6G");
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
