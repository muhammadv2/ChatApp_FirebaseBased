package com.muhammadv2.pm_me.Utils;

import com.google.firebase.database.FirebaseDatabase;

/**
 * A FirebaseApp is initialized by a ContentProvider so it is not initialized at the time onCreate()
 * so one of the solution is to get instance of it and use persistence method in a static method
 */
public class FirebaseUtils {
    private static FirebaseDatabase mDatabase;
    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
}
