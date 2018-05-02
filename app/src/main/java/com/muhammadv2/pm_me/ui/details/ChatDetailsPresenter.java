package com.muhammadv2.pm_me.ui.details;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;
import com.muhammadv2.pm_me.model.AuthUser;
import com.muhammadv2.pm_me.model.Message;

import java.util.List;

class ChatDetailsPresenter extends MvpNullObjectBasePresenter<IChatDetailsView>{


    // Fire base instances
    private DatabaseReference mUsersChatDbRef;
    private DatabaseReference mMessagesDbRef;
    private ChildEventListener mChildListener;
    private StorageReference mStorageRef;

    private List<Message> messageList; // List to hold all the messages retrieved from the db

    private AuthUser mCurrentUser;
    private AuthUser mTargetedUser;

    private String existsPath;

    private void retrieveAndSaveChatData() {
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                instantiateRecyclerView();
            }

            //region unused
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                //Todo called if the data already in the db has been changed
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Todo called when the db data has been deleted
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                //Todo called when the data has been moved to another node (json object in db)
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Todo Called when there's an error most of the time there's no authentication
            }
            //endregion
        };
        mUsersChatDbRef.addChildEventListener(mChildListener);
    }

}
