package com.muhammadv2.pm_me.component.users;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.muhammadv2.pm_me.Utils.FirebaseUtils;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is to show a list of authenticated users by firstly fire
 * {@link #mAuthListener} to find if the current user is registered if not redirect him to sign up
 * screen if yes then load the list of the users by calling this method {@link #loadAllAuthUsers()}
 * and also make sure to add the current user  {@link #addAllUserToTheList(DataSnapshot)}
 */
public class UsersPresenterImp extends UsersPresenter {

    private static final String mUsersNode = "users";

    private DatabaseReference mUsersReference;
    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private List<AuthUser> mAuthUsers;
    private String mCurrentUserKey;
    private AuthUser mCurrentUser;

    UsersPresenterImp() {
        FirebaseDatabase mFireBaseDb = FirebaseUtils.getDatabase();
        mUsersReference = mFireBaseDb.getReference().child(mUsersNode);
        mFbAuth = FirebaseAuth.getInstance();
        mAuthUsers = new ArrayList<>();
    }

    public void loadDataIfUserAuthOrShowSignInScreen() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) { // User authorized
                addTheUserDataToDb(user);
                loadAllAuthUsers();
            } else { // User not authorized
                onSignedOutCleaner();
                getView().showSignIn();
            }
        };
    }

    @Override
    public void getChatUsers(int position) {
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

    /**
     * Extract all the users stored in the database to show them as a list and prevent duplicate
     * the users by checking the unique key before adding
     */
    private void loadAllAuthUsers() {

        mUsersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Called every time data changes and when the first attach happen
                addAllUserToTheList(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Loop on DataSnapshot that holds all message node data
     *
     * @param dataSnapshot by getting the whole children the object holds we can find the current
     *                     user and skip adding him and add only all other users in the list
     */
    private void addAllUserToTheList(DataSnapshot dataSnapshot) {
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
        getView().setData(mAuthUsers);
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
