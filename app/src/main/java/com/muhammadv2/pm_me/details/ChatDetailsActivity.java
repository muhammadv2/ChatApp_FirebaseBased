package com.muhammadv2.pm_me.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muhammadv2.pm_me.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatDetailsActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private static final int RC_PHOTO_PICKER = 909;

    @BindView(R.id.rv_messages_container)
    RecyclerView mMessageRv;

    MessageAdapter mMessageAdapter;

    @BindView(R.id.btn_select_image)
    ImageButton mPhotoPickerButton;

    @BindView(R.id.et_user_input)
    EditText mMessageEditText;

    @BindView(R.id.btn_send_msg)
    Button mSendButton;

    // Fire base instances
    private DatabaseReference mMessageDbRef;
    private ChildEventListener mChildListener;
    private StorageReference mStorageRef;

    private List<Message> messageList; // List to hold all the messages retrieved from the db

    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        ButterKnife.bind(this);

        // Set some restrictions over the user input
        editTextWatcher();

        // Instantiating Fb database entry and creating a child named messages in th db.
        FirebaseDatabase mFireBaseDb = FirebaseDatabase.getInstance();
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();

        mMessageDbRef = mFireBaseDb.getReference().child("messages");
        mStorageRef = mFirebaseStorage.getReference().child("chat_photos");

        instantiateRecyclerView(); //Just setting set has fixed size and the layoutManager on RV

        messageList = new ArrayList<>();

        attachDatabaseListener();

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Populate message model with the input from the user
                Message message =
                        new Message(mUsername,
                                mMessageEditText.getText().toString().trim(), // Message body
                                null);

                // .push() method used to write to the fb db putting the value on messages node
                mMessageDbRef.push().setValue(message);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        // Image picker button send an intent to open photos associated apps
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser
                        (intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });


    }

    private void attachDatabaseListener() {
        if (mChildListener == null){
            // Instantiating listener over the db to get the stored data and listen to any changes
            mChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    messageList.add(dataSnapshot.getValue(Message.class));
                    // Send the list of the messages to the adapter and populate the recycler view
                    mMessageAdapter = new MessageAdapter(ChatDetailsActivity.this, messageList);
                    mMessageRv.setAdapter(mMessageAdapter);
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
        // Attaching the listener to the node Ref
        mMessageDbRef.addChildEventListener(mChildListener);
    }}

    private void detachDatabaseListener() {
        if (mChildListener != null) {
            mMessageDbRef.removeEventListener(mChildListener);
            mChildListener = null;
        }
        if (mMessageAdapter != null)
            mMessageAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            // We now have an image get its uri and then send it to the storage
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                StorageReference photoRef = mStorageRef.child(selectedImage.getLastPathSegment());
                UploadTask uploadTask = photoRef.putFile(selectedImage);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (taskSnapshot.getDownloadUrl() == null) return;
                        // The photo uploaded successfully get its url and push it to the db
                        String mImageUri = taskSnapshot.getDownloadUrl().toString();
                        Message message = new Message(mUsername, null, mImageUri);
                        mMessageDbRef.push().setValue(message);
                    }
                });
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseListener();
    }

    private void instantiateRecyclerView() {
        mMessageRv.setHasFixedSize(true);
        mMessageRv.setLayoutManager(new LinearLayoutManager(this));
    }

    private void editTextWatcher() {

        // Only unable the send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (mMessageEditText.getText().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        // Set maximum length for a single message not exceed 1000.
        mMessageEditText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
    }
}