package com.muhammadv2.pm_me;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ChatActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 305;
    private static final String ANONYMOUS = "Not assigned";
    private String mUsername = ANONYMOUS;

    // Firebase instances
    private FirebaseDatabase mFireBaseDb;
    private DatabaseReference mUsersIdReference;
    private ChildEventListener mUsersListener;
    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private List<AuthUser> mAuthUsers;

    @BindView(R.id.rv_users)
    RecyclerView mUsersRV;
    UsersAdapter mUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

        mAuthUsers = new ArrayList<>();

        mFireBaseDb = FirebaseDatabase.getInstance();
        mUsersIdReference = mFireBaseDb.getReference().child("users");

        mFbAuth = FirebaseAuth.getInstance();

        // Auth listener override method called when the auth change means the user logged in or not
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) { // User authorized
                    // Name to be displayed on the UI and attach the listener to receive the data
                    mUsername = user.getDisplayName();
                    onSignedInInitialize(user);
                    loadAllAuthUsers();

                } else { // User not authorized
                    // Do clear the adapter and detach the listener if the user not logged in
                    onSignedOutCleaner();

                    // Start the Firebase UI for logging in by email and google provider
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.app_icon)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()
                                            // You can add more providers here
                                    ))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }

    private void loadAllAuthUsers() {

        mUsersListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                AuthUser user = dataSnapshot.getValue(AuthUser.class);
                mAuthUsers.add(user);
                Timber.d("current user " + user.getName());
            }

            //region Unused
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
            //endregion
        };
        initializeRecycler();
        mUsersIdReference.addChildEventListener(mUsersListener);
        mUsersAdapter = new UsersAdapter(mAuthUsers);
        mUsersRV.setAdapter(mUsersAdapter);

    }

    private void onSignedInInitialize(FirebaseUser authUser) {
        String userId = authUser.getUid();
        String userName = authUser.getDisplayName();
        String userPhotoUrl = null;
        if (authUser.getPhotoUrl() != null) {
            userPhotoUrl = authUser.getPhotoUrl().toString();
        }

        Timber.d("user id " + userId);

        AuthUser user = new AuthUser(userId, userName, userPhotoUrl);

        mUsersIdReference.push().setValue(user);
    }


    private void onSignedOutCleaner() {
        mUsername = ANONYMOUS;
//        detachDatabaseListener();
    }

    private void detachDatabaseListener() {
//        if (mChildListener != null) {
//            mMessageDbRef.removeEventListener(mChildListener);
//            mChildListener = null;
//        }
        if (mUsersAdapter != null)
            mUsersAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initializeRecycler() {
        mUsersRV.setHasFixedSize(true);
        mUsersRV.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFbAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFbAuth != null)
            mFbAuth.removeAuthStateListener(mAuthListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out_btn:
                AuthUI.getInstance().signOut(this); // Sign out function as simple as that
        }
        return super.onOptionsItemSelected(item);
    }
}
