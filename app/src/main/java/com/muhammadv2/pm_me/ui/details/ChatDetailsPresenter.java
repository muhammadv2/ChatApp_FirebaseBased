package com.muhammadv2.pm_me.ui.details;

import android.net.Uri;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;
import com.muhammadv2.pm_me.Utils.FirebaseUtils;
import com.muhammadv2.pm_me.model.AuthUser;
import com.muhammadv2.pm_me.model.Message;

import java.util.List;
import java.util.function.Consumer;

class ChatDetailsPresenter extends MvpNullObjectBasePresenter<IChatDetailsView> {


    // Fire base instances
    private DatabaseReference mUsersChatDbRef;
    private DatabaseReference mMessagesDbRef;
    private ChildEventListener mChildListener;
    private StorageReference mStorageRef;

    private List<Message> messageList; // List to hold all the messages retrieved from the db

    private AuthUser mCurrentUser;
    private AuthUser mTargetedUser;

    private String existsPath;

    void loadData(Consumer<List<Message>> onNewResult){

    }

    private void updateThePathAccordingIfExistOrNot() {
        mMessagesDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    String singleSnapKey = singleSnap.getKey(); // save the coming keys
                    // check if the key contains both users keys
                    if (checkIfPathContainsBothKeys(singleSnapKey)) {
                        existsPath = singleSnap.getKey(); // if yes update the exists path
                        break;
                    }
                }
                if (existsPath == null) {
                    // if the existsPath not updated specify a new one by combining the users keys
                    existsPath = mCurrentUser.getUid() + "-" + mTargetedUser.getUid();
                }
                mUsersChatDbRef = FirebaseUtils.getDatabase().getReference()
                        .child(MESSAGES_NODE_DB)
                        .child(existsPath);  //finally looks like /messages/24L0kQx7506-2XK48YsESFaQ

                retrieveAndSaveChatData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private boolean checkIfPathContainsBothKeys(String singleKey) {
        return !singleKey.isEmpty() && singleKey.contains(mCurrentUser.getUid())
                && singleKey.contains(mTargetedUser.getUid());
    }


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

    public void handleSelectedImage(Uri selectedImage) {
        // We now have an image get its uri and then send it to the storage

        if (selectedImage != null) {
            StorageReference photoRef = mStorageRef.child(selectedImage.getLastPathSegment());
            UploadTask uploadTask = photoRef.putFile(selectedImage);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getDownloadUrl() == null) return;
                // The photo uploaded successfully get its url and push it to the db
                String mImageUri = taskSnapshot.getDownloadUrl().toString();
                Message message = new Message(mCurrentUser.getName(),
                        null,
                        mImageUri);
                mUsersChatDbRef.push().setValue(message);
            });
        }
    }

}
