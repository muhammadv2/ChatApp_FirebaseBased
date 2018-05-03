package com.muhammadv2.pm_me.ui.details;

import android.net.Uri;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;
import com.muhammadv2.pm_me.Utils.FirebaseUtils;
import com.muhammadv2.pm_me.model.AuthUser;
import com.muhammadv2.pm_me.model.Message;

import java.util.ArrayList;
import java.util.List;

import java9.util.function.Consumer;

import static com.muhammadv2.pm_me.Constants.MESSAGES_NODE_DB;
import static com.muhammadv2.pm_me.Constants.PHOTOS_DATA_STORAGE;

class ChatDetailsPresenter extends MvpNullObjectBasePresenter<IChatDetailsView> {

    // Fire base instances
    private DatabaseReference mUsersChatDbRef;
    private DatabaseReference mMessagesDbRef;
    private ChildEventListener mChildListener;
    private StorageReference mStorageRef;

    private AuthUser mCurrentUser;
    private AuthUser mTargetedUser;

    private String existsPath;

    ChatDetailsPresenter() {
        mMessagesDbRef = FirebaseUtils.getDatabase().getReference().child(MESSAGES_NODE_DB);
        mStorageRef = FirebaseStorage.getInstance().getReference().child(PHOTOS_DATA_STORAGE);
    }

    void loadData(Consumer<List<Message>> onNewResult) {

        mMessagesDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnap : dataSnapshot.getChildren()) {
                    if (checkIfPathContainsBothKeys(singleSnap.getKey())) {
                        //Current users have running chat path so update it
                        existsPath = singleSnap.getKey();
                        break;
                    }
                }
                if (existsPath == null) {
                    //Current users never chatted before create a new path by combining they keys
                    existsPath = mCurrentUser.getUid() + "-" + mTargetedUser.getUid();
                }
                mUsersChatDbRef = FirebaseUtils.getDatabase().getReference()
                        .child(MESSAGES_NODE_DB)
                        .child(existsPath); //finally looks like /messages/24L0kQx7506-2XK48YsESFaQ

                retrieveAndSaveChatData(onNewResult);
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

    private void retrieveAndSaveChatData(Consumer<List<Message>> onNewResult) {
        List<Message> messages = new ArrayList<>();
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messages.add(message);
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
        onNewResult.accept(messages);
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

    void detachListeners() {
        if (mChildListener != null) {
            mUsersChatDbRef.removeEventListener(mChildListener);
            mChildListener = null;
        }
    }

    public void sendButtonClicked(String messageBody) {
        Message message = new Message(mCurrentUser.getName(), messageBody, null);
        mUsersChatDbRef.push().setValue(message);
    }
}
