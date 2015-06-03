package com.parche.partnerurlschemesample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;
import butterknife.*;
import com.parche.helperlib.ParchePartnerURLSchemeHelper;

public class MainActivity extends ActionBarActivity implements StagingDiscountGenerator.StagingDiscountListener {

    @InjectView(R.id.can_open_textview) TextView mCanOpenTextView;

    ProgressDialog mProgressDialog;

    /**********************
     * ACTIVITY LIFECYCLE *
     **********************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    /**********
     * ALERTS *
     **********/

    private void showAlert(@StringRes int aTitleResourceID, @StringRes int aMessageResourceID) {
        new AlertDialog.Builder(this)
                .setTitle(aTitleResourceID)
                .setMessage(aMessageResourceID)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showAlert(String aTitle, String aMessage) {
        new AlertDialog.Builder(this)
                .setTitle(aTitle)
                .setMessage(aMessage)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void showNotInstalledAlert() {
        showAlert(R.string.not_installed_alert_title, R.string.not_installed_alert_message);
    }

    /*********************
     * ONCLICK LISTENERS *
     *********************/

    @OnClick(R.id.check_install_button)
    public void checkParcheNeedsUpdateOrInstall() {
        boolean needs = ParchePartnerURLSchemeHelper.parcheNeedsToBeUpdatedOrInstalled(this);
        String needsText = needs ? "YES" : "NO";
        mCanOpenTextView.setText(needsText);
    }

    @OnClick(R.id.show_in_store_button)
    public void showParcheInPlayStore() {
        Intent openPlayStoreIntent = ParchePartnerURLSchemeHelper.showParcheInPlayStoreIntent(this);
        startActivity(openPlayStoreIntent);
    }

    @OnClick(R.id.open_no_discount_button)
    public void openWithoutDiscount() {
        Intent openIntent = ParchePartnerURLSchemeHelper.openParcheIntent(this, "FAKE_API_KEY");
        if (openIntent == null) {
            showNotInstalledAlert();
        } else {
            startActivity(openIntent);
        }
    }

    @OnClick(R.id.open_fake_discount_button)
    public void openWithFakeDiscount() {
        Intent openIntent = ParchePartnerURLSchemeHelper.openParcheAndRequestDiscount(this, "FAKE_DISCOUNT_CODE", "Partner User ID", "FAKE_API_KEY");
        if (openIntent == null) {
            showNotInstalledAlert();
        } else {
            startActivity(openIntent);
        }
    }

    @OnClick(R.id.open_staging_discount_button)
    public void openWithStagingDiscount() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.getting_discount);
        mProgressDialog.show();
        StagingDiscountGenerator generator = new StagingDiscountGenerator(this);
        generator.execute();
    }

    /*****************************
     * STAGING DISCOUNT LISTENER *
     *****************************/

    @Override
    public void errorGettingDiscount(String errorDescription) {
        mProgressDialog.hide();
        showAlert("ERRORZ", errorDescription);
    }

    @Override
    public void gotDiscount(String aDiscount, String aAPIKey, String aUsername) {
        mProgressDialog.hide();
        Intent openWithStagingDiscountIntent = ParchePartnerURLSchemeHelper.openParcheAndRequestDiscount(this, aDiscount, aUsername, aAPIKey);
        if (openWithStagingDiscountIntent == null) {
            showNotInstalledAlert();
        } else {
            startActivity(openWithStagingDiscountIntent);
        }
    }
}

