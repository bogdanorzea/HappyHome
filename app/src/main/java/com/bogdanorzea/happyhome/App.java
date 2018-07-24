package com.bogdanorzea.happyhome;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Enable Firebase persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
