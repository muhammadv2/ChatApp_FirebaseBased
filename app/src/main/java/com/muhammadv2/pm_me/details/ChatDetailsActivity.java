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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.muhammadv2.pm_me.Utils.FirebaseUtils;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.main.AuthUser;
import com.muhammadv2.pm_me.main.UsersActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * This class is to show messages between users separately the logic is a bit complicated but this
 * was is the best i could get
 * <p>
 * 1. {@link #updateThePathAccordingIfExistOrNot()} First we have to listen to the exists messages
 * node in the database to find if the keys from current user and targeted user exists there if it
 * exists we will keep the key to use it again to push and not duplicate keys if not will simply
 * create a new one
 * <p>
 * 2. {@link #retrieveAndSaveChatData()} Will call this method after making sure we have a path
 * either new or exist and will populate the message object directly with the coming data no need
 * to iterate because {@link #mChildListener} read the node children directly
 * <p>
 * 3. Finally push when the send button is clicked if a normal message and when back from picking
 * and image basically by using our updated path of {@link #mUsersChatDbRef}
 */
public class ChatDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_PHOTO_PICKER = 909;
    public static final String MESSAGES_NODE_DB = "messages";
    public static final String PHOTOS_DATA_STORAGE = "chat_photos";

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
    private DatabaseReference mUsersChatDbRef;
    private DatabaseReference mMessagesDbRef;
    private ChildEventListener mChildListener;
    private StorageReference mStorageRef;

    private List<Message> messageList; // List to hold all the messages retrieved from the db

    private AuthUser mCurrentUser;
    private AuthUser mTargetedUser;

    private String existsPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());
        mPhotoPickerButton.setOnClickListener(this);
        mSendButton.setOnClickListener(this);

        messageList = new ArrayList<>();
        // Set some restrictions over the user input

        editTextWatcher();

        saveDataComingWithIntent();


        mMessagesDbRef = FirebaseUtils.getDatabase().getReference().child(MESSAGES_NODE_DB);

        mStorageRef = FirebaseStorage.getInstance().getReference().child(PHOTOS_DATA_STORAGE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateThePathAccordingIfExistOrNot();
    }

    private void saveDataComingWithIntent() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        mCurrentUser = bundle.getParcelable(UsersActivity.CURRENT_USER_DATA);
        mTargetedUser = bundle.getParcelable(UsersActivity.TARGETED_USER_DATA);
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

    private void instantiateRecyclerView() {
        mMessageRv.setHasFixedSize(true);
        mMessageRv.setLayoutManager(new LinearLayoutManager(this));
        mMessageAdapter = new MessageAdapter(messageList);
        mMessageRv.setAdapter(mMessageAdapter);
    }

    private void detachDatabaseListener() {
        if (mChildListener != null) {
            mUsersChatDbRef.removeEventListener(mChildListener);
            mChildListener = null;
        }
        if (mMessageAdapter != null)
            mMessageAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
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
                        Message message = new Message(mCurrentUser.getName(),
                                null,
                                mImageUri);
                        mUsersChatDbRef.push().setValue(message);
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btn_select_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser
                        (intent, "Complete action using"), RC_PHOTO_PICKER);
                break;
            case R.id.btn_send_msg:
                // Populate message model with the input from the user
                Message message =
                        new Message(mCurrentUser.getName(),
                                mMessageEditText.getText().toString().trim(), // Message body
                                null);

                mUsersChatDbRef.push().setValue(message);
                // Clear input box
                mMessageEditText.setText("");
                break;
            default:
                Timber.d("Not recognized view");
        }
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

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseListener();
    }
}
