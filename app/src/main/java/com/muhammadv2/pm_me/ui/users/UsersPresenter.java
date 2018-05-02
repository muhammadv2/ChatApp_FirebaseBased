package com.muhammadv2.pm_me.ui.users;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;
import com.muhammadv2.pm_me.Utils.FirebaseUtils;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.ArrayList;
import java.util.List;

import java9.util.function.Consumer;

public class UsersPresenter extends MvpNullObjectBasePresenter<IUsersView> {

    private static final String mUsersNode = "users";

    private DatabaseReference mUsersReference;
    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private List<AuthUser> mAuthUsers;
    private String mCurrentUserKey;
    private AuthUser mCurrentUser;

    UsersPresenter() {
        FirebaseDatabase mFireBaseDb = FirebaseUtils.getDatabase();
        mUsersReference = mFireBaseDb.getReference().child(mUsersNode);
        mFbAuth = FirebaseAuth.getInstance();
        mAuthUsers = new ArrayList<>();
    }

    public void loadData(Consumer<List<AuthUser>> onNewResult) {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) { // User authorized
                addTheUserDataToDb(user);
                loadAllAuthUsers(onNewResult);
            } else { // User not authorized
                onSignedOutCleaner();
                getView().showSignIn();
            }
        };
    }

    public void onTargetUserClicked(int position) {
        getView().navigateChatDetails(mCurrentUser, mAuthUsers.get(position));
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

    private void loadAllAuthUsers(Consumer<List<AuthUser>> onNewResult) {

        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Called every time data changes and when the first attach happen
                mAuthUsers.clear(); // First clear all data to prevent duplication
                for (DataSnapshot singleChild : dataSnapshot.getChildren()) {
                    if (!mCurrentUserKey.equals(singleChild.getKey())) {
                        AuthUser authUser = singleChild.getValue(AuthUser.class);
                        if (authUser != null)
                            authUser.setUid(singleChild.getKey());
                        mAuthUsers.add(authUser);
                    } else {
                        mCurrentUser = singleChild.getValue(AuthUser.class);
                        mCurrentUser.setUid(singleChild.getKey());
                        getView().showCurrentUserInfo(mCurrentUser.getName(), mCurrentUser.getImageUrl());
                    }
                }
                onNewResult.accept(mAuthUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void onSignedOutCleaner() {
        getView().clearAdapter();
    }


    public void setAuthStateListener() {
        mFbAuth.addAuthStateListener(mAuthListener);
    }

    public void detachListeners() {
        if (mFbAuth != null)
            mFbAuth.removeAuthStateListener(mAuthListener);
    }
}
