package com.muhammadv2.pm_me;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    @BindView(R.id.rv_messages_container)
    RecyclerView mMessageRv;

    MessageAdapter mMessageAdapter;

    @BindView(R.id.btn_select_image)
    ImageButton mPhotoPickerButton;

    @BindView(R.id.et_user_input)
    EditText mMessageEditText;

    @BindView(R.id.btn_send_msg)
    Button mSendButton;

    private FirebaseDatabase mFireBaseDb;
    private DatabaseReference mMessageDbRef;
    private ChildEventListener mChildListener;

    private List<Message> messageList; // List to hold all the messages retrieved from the db


    private ProgressBar mProgressBar;

    private String mUsername = "No One";

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

        instantiateRecyclerView(); //Just setting set has fixed size and the layoutManager on RV

        messageList = new ArrayList<>();

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Populate message model with the input from the user
                Message message =
                        new Message(mUsername,
                                mMessageEditText.getText().toString().trim(),
                                null);

                // .push() method used to write to the fb db setting the value on messages node
                mMessageDbRef.push().setValue(message);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        // Instantiating listener over the db to listen if there any data has been changed
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                messageList.add(dataSnapshot.getValue(Message.class));
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

        // Send the list of the messages to the adapter and populate the recycler view
        mMessageAdapter = new MessageAdapter(messageList);
        mMessageRv.setAdapter(mMessageAdapter);

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
