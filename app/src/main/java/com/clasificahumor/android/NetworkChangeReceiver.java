package com.clasificahumor.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Santiago on 26/08/2014.
**/
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        context.sendBroadcast(new Intent(MainActivity.CONNECTIVITY_CHANGE_INTENT_NAMESPACE));
    }
}
