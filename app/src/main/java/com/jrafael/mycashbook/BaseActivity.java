package com.jrafael.mycashbook;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.script.model.ExecutionRequest;
import com.jrafael.mycashbook.dummy.DummyContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * A BaseActivity that provides access App Script by:
 * <li>Initialize credentials and service object</li>
 * <li>Select account</li>
 * <li>Check Internet conection</li>
 * <li>Ask authorization</li>
 * <p/>
 * Also give to methods that Request for App Script function.
 * @link getResumen
 * @ling appendRow
 *
 */
public class BaseActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks{

    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    private static final String[] SCOPES = {
            "https://www.googleapis.com/auth/drive"
            ,"https://www.googleapis.com/auth/spreadsheets" };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize credentials and service object.
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Apps Script Execution API ...");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }




    /**
     *  Interface observe de result of GetResumen invocations.
     *  It's as a callback of asincronic programing.
     */
    public interface ListenerGetResumen {
        void onGetResumen(List<DummyContent.DummyItem> items);
    }

    /**
     * Internal reference.
     */
    private ListenerGetResumen resulter;

    /**
     * Set the Listener.
     * @param resulter the new listener.
     */
    public void setOnListenerGetResumen(ListenerGetResumen resulter) {
        this.resulter = resulter;
    }
    /**
     * Call App script with the same name.
     *
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    protected void getResumen() {
        new MakeRequestTask(mCredential,
                new MakeRequestTask.AppScriptRequestHandler() {


                    public void onPreExecute(ExecutionRequest request) {
                        mProgress.show();
                        request.setFunction("getResumen");
                    }

                    public void onPostExecute(Map<String, Object> response) {
                        mProgress.hide();
                        List<DummyContent.DummyItem> folderList = new ArrayList<DummyContent.DummyItem>();

                        if (response != null &&
                                response.get("result") != null) {
                            // The result provided by the API needs to be cast into
                            // the correct type, based upon what types the Apps Script
                            // function returns. Here, the function returns an Apps
                            // Script Object with String keys and values, so must be
                            // cast into a Java Map (folderSet).
                            List<List<?>> result =
                                    (List<List<?>>)(response.get("result"));
                            result.set(0, Arrays.asList(new Object[]{"","",""}));

                            for (List<?> row: result) {
                                folderList.add(new DummyContent.DummyItem(row.get(0).toString(),row.get(1).toString(),row.get(2).toString()));
                            }
                        }

                        resulter.onGetResumen(folderList);
                    }


                    public void onCancelled(Exception mLastError) {
                        mProgress.hide();

                        if (mLastError != null) {
                            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                                showGooglePlayServicesAvailabilityErrorDialog(
                                        ((GooglePlayServicesAvailabilityIOException) mLastError)
                                                .getConnectionStatusCode());
                            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                                startActivityForResult(
                                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                                        BaseActivity.REQUEST_AUTHORIZATION);
                            } else {
                                new AlertDialog.Builder(BaseActivity.this).setMessage(
                                        "The following error occurred:\n"
                                                + mLastError.getMessage()
                                ).show();
                            }
                        } else {
                            new AlertDialog.Builder(BaseActivity.this).setMessage(
                                    "Request cancelled."
                            ).show();
                        }
                    }
                }

        ).execute();

    }

    /**
     * Call App script with the same name.
     *
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    protected void appendRow(final Object[] parametres) {

            new MakeRequestTask(mCredential,
                    new MakeRequestTask.AppScriptRequestHandler() {


                        public void onPreExecute(ExecutionRequest request) {
                            mProgress.show();
                            request.setFunction("appendRow")
                                    .setParameters(Arrays.asList(new Object[]{
                                            Arrays.asList(parametres)
                                    }));
                        }

                        public void onPostExecute(Map<String, Object> getResponse) {
                            mProgress.hide();

                        }


                        public void onCancelled(Exception mLastError) {
                            mProgress.hide();

                            if (mLastError != null) {
                                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                                    showGooglePlayServicesAvailabilityErrorDialog(
                                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                                    .getConnectionStatusCode());
                                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                                    startActivityForResult(
                                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                                            BaseActivity.REQUEST_AUTHORIZATION);
                                } else {
                                    new AlertDialog.Builder(BaseActivity.this).setMessage(
                                            "The following error occurred:\n"
                                                    + mLastError.getMessage()
                                    ).show();

                                }
                            } else {
                                new AlertDialog.Builder(BaseActivity.this).setMessage(
                                        "Request cancelled."
                                ).show();

                            }
                        }
                    }

            ).execute();

    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    protected void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            new AlertDialog.Builder(BaseActivity.this).setMessage(
                    "No network connection available."
            ).show();

        } else {
            //Default function.
           getResumen();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {

        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    new AlertDialog.Builder(this).setMessage(
                            "This app requires Google Play Services. Please install " +
                                       "Google Play Services on your device and relaunch this app."
                    ).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                BaseActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }


}
