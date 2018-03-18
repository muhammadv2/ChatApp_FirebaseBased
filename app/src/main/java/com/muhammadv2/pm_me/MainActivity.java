package com.muhammadv2.pm_me;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_SIGN_IN = 305;
    private static final String ANONYMOUS = "Not assigned";

    @BindView(R.id.rv_messages_container)
    RecyclerView mMessageRv;

    MessageAdapter mMessageAdapter;

    @BindView(R.id.btn_select_image)
    ImageButton mPhotoPickerButton;

    @BindView(R.id.et_user_input)
    EditText mMessageEditText;

    @BindView(R.id.btn_send_msg)
    Button mSendButton;

    // Firebase instance variables
    private FirebaseDatabase mFireBaseDb;
    private DatabaseReference mMessageDbRef;
    private ChildEventListener mChildListener;
    private FirebaseAuth mFbAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private List<Message> messageList; // List to hold all the messages retrieved from the db

    private ProgressBar mProgressBar;

    private String mUsername = ANONYMOUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Set some restrictions over the user input
        editTextWatcher();

        // Instantiating Fb database entry and creating a child named messages in th db.
        mFireBaseDb = FirebaseDatabase.getInstance();
        mMessageDbRef = mFireBaseDb.getReference().child("messages");
        mFbAuth = FirebaseAuth.getInstance();

        instantiateRecyclerView(); //Just setting set has fixed size and the layoutManager on RV

        messageList = new ArrayList<>();

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Populate message model with the input from the user
                Message message =
                        new Message(mUsername,
                                mMessageEditText.getText().toString().trim(), // Message body
                                null);

                // .push() method used to write to the fb db setting the value on messages node
                mMessageDbRef.push().setValue(message);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        // Auth listener override method called when the auth change means the user logged in or not
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) { // User authorized
                    Toast.makeText(MainActivity.this,
                            "You're logged in successfully",
                            Toast.LENGTH_LONG).show();

                    //name to be displayed on the UI and attach the listener to receive the data
                    onSignedInInitialize(user.getDisplayName());
                } else { // User not authorized
                    // Do clear the adapter and detach the listener if the user not logged in
                    onSignedOutCleaner();

                    // Start the Firebase UI for logging in by email and google provider
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
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

    private void onSignedInInitialize(String displayName) {
        mUsername = displayName;
        attachDatabaseListener();
    }

    private void onSignedOutCleaner() {
        mUsername = ANONYMOUS;
        detachDatabaseListener();
    }

    private void attachDatabaseListener() {
        if (mChildListener == null)
            // Instantiating listener over the db to get the stored data and listen to any changes
            mChildListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    messageList.add(dataSnapshot.getValue(Message.class));
                    // Send the list of the messages to the adapter and populate the recycler view
                    mMessageAdapter = new MessageAdapter(messageList);
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
    }

    private void detachDatabaseListener() {
        if (mChildListener != null) {
            mMessageDbRef.removeEventListener(mChildListener);
            mMessageAdapter.clear();
            mChildListener = null;
        }
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
