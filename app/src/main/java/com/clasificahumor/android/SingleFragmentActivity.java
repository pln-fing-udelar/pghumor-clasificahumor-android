package com.clasificahumor.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by santiago on 16/09/14.
**/
public abstract class SingleFragmentActivity extends FragmentActivity {

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

    public static final String CONNECTIVITY_CHANGE_INTENT_NAMESPACE = MainActivity.class.getPackage().getName() + ".CONNECTIVITY_CHANGE";

    private IntentFilter mIntentFilter = new IntentFilter(CONNECTIVITY_CHANGE_INTENT_NAMESPACE);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            verifyConnectionInUi(context);
        }
    };

    public static void verifyConnectionInUi(final Context context) {
        if (context instanceof Activity) {
            Crouton.cancelAllCroutons();

            if (!isConnected(context)) {
                Configuration configuration = new Configuration.Builder()
                        .setDuration(Configuration.DURATION_INFINITE)
                        .build();
                Style style = new Style.Builder()
                        .setBackgroundColorValue(Style.holoRedLight)
                        .setConfiguration(configuration)
                        .build();
                Crouton.makeText((Activity) context, "No hay conexión a Internet", style).show();
            }
        }
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    protected abstract Fragment createFragment();

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);

        registerReceiver(mReceiver, mIntentFilter);

        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }

        final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        } else if (statusCode == ConnectionResult.SERVICE_MISSING ||
                statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                statusCode == ConnectionResult.SERVICE_DISABLED) {
            showGooglePlayServicesAvailabilityErrorDialog(statusCode);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        verifyConnectionInUi(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        verifyConnectionInUi(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Crouton.cancelAllCroutons(); // It's already in onPause.
        unregisterReceiver(mReceiver);

        final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            GoogleAnalytics.getInstance(this).reportActivityStop(this);
        }
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int statusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, SingleFragmentActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
                if (dialog != null) {
                    dialog.show();
                }
            }
        });
    }
}
