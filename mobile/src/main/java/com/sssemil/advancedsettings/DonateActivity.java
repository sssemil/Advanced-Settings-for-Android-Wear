package com.sssemil.advancedsettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.sssemil.advancedsettings.util.IabHelper;
import com.sssemil.advancedsettings.util.IabResult;
import com.sssemil.advancedsettings.util.Inventory;
import com.sssemil.advancedsettings.util.Purchase;

public class DonateActivity extends Activity implements Button.OnClickListener {

    IabHelper mHelper;

    String Base64EncodedPublicKey =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqWiRHT9nfqRW4Yh4pfhk/cTQ5LvOnzQFpbAhNTl+sHqmPV6PkyMCtXErUg8alqVgAFJWMJnmBSS08gtCIHifo6TFHIMP0HBZPoPH674g02bPYBpdlN7Nymx4t9yVnswv/Auf1Lqvvdwrm4EM8fjRCEQjzds4KTYDPmrcgYhT8lyRRrqjD+jjkkwDSWTz/iDuhDAclS0J0Vnd63CywJIg6FkNNIzAn473mOSz/HilIkY5GFKTwKChsAGIZAFVO/mKeeX3WT7CDv8FMD4Kec8fCkOD40Uc5LldVf2j76bxb72rGPXWBUhpcllQTHxUkVMooaArJCutzPXf5yvUdoSPlwIDAQAB";

    static final String ITEM_SKU$3 = "great_donation";
    static final String ITEM_SKU$7 = "usd7";
    static final String ITEM_SKU$16 = "usd16";
    static final String ITEM_SKU$32 = "usd32";
    private Button usd3, usd7, usd16, usd32;

    private String CURRENT_SKU;

    public void consumeItem(String sku) {
        CURRENT_SKU = sku;
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(CURRENT_SKU),
                        mConsumeFinishedListener);
            }
        }
    };


    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                return;
            } else if (purchase.getSku().equals(ITEM_SKU$3)) {
                consumeItem(purchase.getSku());
                usd3.setEnabled(false);
            } else if (purchase.getSku().equals(ITEM_SKU$7)) {
                consumeItem(purchase.getSku());
                usd7.setEnabled(false);
            } else if (purchase.getSku().equals(ITEM_SKU$16)) {
                consumeItem(purchase.getSku());
                usd16.setEnabled(false);
            } else if (purchase.getSku().equals(ITEM_SKU$32)) {
                consumeItem(purchase.getSku());
                usd32.setEnabled(false);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {
                    if (result.isSuccess()) {
                        usd3.setEnabled(true);
                        usd7.setEnabled(true);
                        usd16.setEnabled(true);
                        usd32.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View promptsView = layoutInflater.inflate(R.layout.donations, null);

        usd3 = (Button) promptsView.findViewById(R.id.usd3);
        usd7 = (Button) promptsView.findViewById(R.id.usd7);
        usd16 = (Button) promptsView.findViewById(R.id.usd16);
        usd32 = (Button) promptsView.findViewById(R.id.usd32);

        usd3.setOnClickListener(this);
        usd7.setOnClickListener(this);
        usd16.setOnClickListener(this);
        usd32.setOnClickListener(this);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb.setTitle(getString(R.string.app_name));
        adb.setView(promptsView);
        adb.show();

        mHelper = new IabHelper(this, Base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result) {
                                           if (!result.isSuccess()) {
                                               Log.d("TAG", "In-app Billing setup failed: " +
                                                       result);
                                           } else {
                                               Log.d("TAG", "In-app Billing is set up OK");
                                           }
                                       }
                                   });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        final Button button = (Button) v;
        button.getId();
        Uri uri;
        Intent intent;
        switch (button.getId()) {
            case (R.id.usd3):
                mHelper.launchPurchaseFlow(this, ITEM_SKU$3, 10001,
                        mPurchaseFinishedListener, ITEM_SKU$3);
                break;
            case (R.id.usd7):
                mHelper.launchPurchaseFlow(this, ITEM_SKU$7, 10001,
                        mPurchaseFinishedListener, ITEM_SKU$7);
                break;
            case (R.id.usd16):
                mHelper.launchPurchaseFlow(this, ITEM_SKU$16, 10001,
                        mPurchaseFinishedListener, ITEM_SKU$16);
                break;
            case (R.id.usd32):
                mHelper.launchPurchaseFlow(this, ITEM_SKU$32, 10001,
                        mPurchaseFinishedListener, ITEM_SKU$32);
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
