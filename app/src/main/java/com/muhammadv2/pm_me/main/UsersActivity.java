package com.muhammadv2.pm_me.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammadv2.pm_me.Utils.FancyGifDialogCreator;
import com.muhammadv2.pm_me.Utils.FirebaseUtils;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.Utils.GlideImageHandlingUtils;
import com.muhammadv2.pm_me.details.ChatDetailsActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

//Todo when clone this project you have to add your own json file from your google account you can
//get that easily by creating a new project in the app folder
// Todo  Also you have to add your key for firebase in the manifest

public class UsersActivity extends AppCompatActivity implements UsersAdapter.OnItemClickLister
        , View.OnClickListener {

    private static final int RC_SIGN_IN = 305;
    private static final String mUsersNode = "users";
    public static final String CURRENT_USER_DATA = "currentUid";
    public static final String TARGETED_USER_DATA = "chooseUid";

    private DatabaseReference mUsersReference;
    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private List<AuthUser> mAuthUsers;
    private String mCurrentUserKey;
    private AuthUser mCurrentUser;

    @BindView(R.id.rv_users)
    RecyclerView mUsersRV;
    UsersAdapter mUsersAdapter;
    @BindView(R.id.iv_current_user_img)
    ImageView mIvUserImage;
    @BindView(R.id.tv_current_user_name)
    TextView mTvUserName;
    @BindView(R.id.current_user_container)
    LinearLayout mContainerUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ButterKnife.bind(this);

        Timber.plant(new Timber.DebugTree());

        mAuthUsers = new ArrayList<>();

        FirebaseDatabase mFireBaseDb = FirebaseUtils.getDatabase();
        mUsersReference = mFireBaseDb.getReference().child(mUsersNode);
        mFbAuth = FirebaseAuth.getInstance();

        // Listener for Authentication changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) { // User authorized
                    addTheUserDataToDb(user);
                    loadAllAuthUsers();
                } else { // User not authorized
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
        mContainerUserData.setOnClickListener(this);
    }

    /**
     * @param authUser we extract the user info as user id and name and then populate the AuthUser
     *                 Object and send it to the database
     */
    private void addTheUserDataToDb(FirebaseUser authUser) {
        mCurrentUserKey = authUser.getUid();
        String userName = authUser.getDisplayName();
        String userPhotoUrl = null;
        if (authUser.getPhotoUrl() != null) {
            userPhotoUrl = authUser.getPhotoUrl().toString();
        }
        AuthUser user = new AuthUser(userName, userPhotoUrl);

        // To uniquely store the user once with no duplication , Store the user unique id as a key
        // in the node then store the name and the location of the image url as values to this key
        //  /"users"-
        //           |- "userId"-
        //                       |- "userName"
        //                       |- "imageUrl"
        mUsersReference.child(mCurrentUserKey).setValue(user);
    }

    /**
     * Extract all the users stored in the database to show them as a list and prevent duplicate
     * the users by checking the unique key before adding
     */
    private void loadAllAuthUsers() {

        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Called every time data changes and when the first attach happen
                mAuthUsers.clear(); // First clear all data to prevent duplication
                addAllUserToTheList(dataSnapshot);
                initAndPopulateRv();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UsersActivity.this, R.string.error_load_users,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Loop on DataSnapshot that holds all the node data
     *
     * @param dataSnapshot by getting the whole children the object holds we can find the current
     *                     user and skip adding him and add only all other users in the list
     */
    private void addAllUserToTheList(DataSnapshot dataSnapshot) {

        for (DataSnapshot singleChild : dataSnapshot.getChildren()) {
            if (!mCurrentUserKey.equals(singleChild.getKey())) {
                AuthUser authUser = singleChild.getValue(AuthUser.class);
                if (authUser != null)
                    authUser.setUid(singleChild.getKey());
                mAuthUsers.add(authUser);
            } else {
                mCurrentUser = singleChild.getValue(AuthUser.class);
                mCurrentUser.setUid(singleChild.getKey());
                setCurrentUserData();
            }
        }
    }

    private void setCurrentUserData() {
        mTvUserName.setText(mCurrentUser.getName());

        GlideImageHandlingUtils.loadImageIntoView(
                UsersActivity.this,
                mCurrentUser.getImageUrl(),
                mIvUserImage);
    }

    private void initAndPopulateRv() {
        mUsersRV.setHasFixedSize(true);
        mUsersRV.setLayoutManager(new LinearLayoutManager(this));
        mUsersAdapter = new UsersAdapter(mAuthUsers, this);
        mUsersRV.setAdapter(mUsersAdapter);
    }

    private void onSignedOutCleaner() {
        detachDatabaseListener();
    }

    private void detachDatabaseListener() {
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
        detachDatabaseListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    public void onClick(int position) {
        AuthUser currentUser = mAuthUsers.get(position);
        Bundle bundle = new Bundle();

        bundle.putParcelable(TARGETED_USER_DATA, currentUser);
        bundle.putParcelable(CURRENT_USER_DATA, mCurrentUser);

        Intent intent = new Intent(this, ChatDetailsActivity.class);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        switch (viewId) {
            case R.id.current_user_container:
                FancyGifDialogCreator.createFancyAlertDialog(this, mCurrentUser.getName());
        }
    }
}
