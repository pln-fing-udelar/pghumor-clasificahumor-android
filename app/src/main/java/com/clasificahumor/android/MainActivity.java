package com.clasificahumor.android;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    private MainFragment fragment;

    @Override
    protected Fragment createFragment() {
        fragment = new MainFragment();
        return fragment;
    }

    @Override
    protected void onConnected() {
        if (fragment != null) {
            fragment.onConnected();
        }
    }
}
