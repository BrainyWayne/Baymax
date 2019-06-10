package com.francis.proxima;

import com.google.firebase.database.FirebaseDatabase;

/**
 * *
 * * Francis Eshun 2019
 */

public class FirebaseApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
