package com.muhammadv2.pm_me.ui.details;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;
import com.muhammadv2.pm_me.Constants;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.model.Message;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.muhammadv2.pm_me.Constants.RC_PHOTO_PICKER;

public class ChatDetailsFragment extends
        MvpLceFragment<CoordinatorLayout, List<Message>, IChatDetailsView, ChatDetailsPresenter>
        implements IChatDetailsView {

    @BindView(R.id.rv_messages_container)
    RecyclerView mMessageRv;
    MessageAdapter mMessageAdapter;

    @BindView(R.id.btn_select_image)
    ImageButton mPhotoPickerButton;
    @BindView(R.id.et_user_input)
    EditText mMessageEditText;
    @BindView(R.id.btn_send_msg)
    Button mSendButton;

    private Context mContext = getContext();
    private Activity mActivity;

    public ChatDetailsFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public ChatDetailsPresenter createPresenter() {
        return new ChatDetailsPresenter();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRetainInstance(true);
        ButterKnife.bind(this, view);
        Timber.plant(new Timber.DebugTree());
        mSendButton.setEnabled(false);
        mActivity = getActivity();
        saveDataComingWithIntent();

        loadData(false);

        // Set some restrictions over the user input
        editTextWatcher();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        presenter.loadData(this::attachView);
    }

    private void attachView(List<Message> messages) {
        mMessageRv.setHasFixedSize(true);
        mMessageRv.setLayoutManager(new LinearLayoutManager(mContext));
        mMessageAdapter = new MessageAdapter(messages);
        mMessageRv.setAdapter(mMessageAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        detachDatabaseListener();
    }

    private void saveDataComingWithIntent() {
        Intent intent = mActivity.getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;
        mCurrentUser = bundle.getParcelable(Constants.CURRENT_USER_DATA);
        mTargetedUser = bundle.getParcelable(Constants.TARGETED_USER_DATA);
    }

    private void detachDatabaseListener() {
        if (mChildListener != null) {
            mUsersChatDbRef.removeEventListener(mChildListener);
            mChildListener = null;
        }
        if (mMessageAdapter != null)
            mMessageAdapter.clear();
    }

    //region View input Methods
    public void onClick(View view) {

        mPhotoPickerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser
                    (intent, "Complete action using"), RC_PHOTO_PICKER);
        });
        mSendButton.setOnClickListener(v -> {
            // Populate message model with the input from the user
            Message message =
                    new Message(mCurrentUser.getName(),
                            mMessageEditText.getText().toString().trim(), // Message body
                            null);

            mUsersChatDbRef.push().setValue(message);
            // Clear input box
            mMessageEditText.setText("");

            // Check if no view has focus:
            View focusedView = mActivity.getCurrentFocus();
            if (focusedView != null) {
                InputMethodManager imm = (InputMethodManager)
                        mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
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

    //endregion
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatDetailsFragment.RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                handleSelectedImage(selectedImage);
            }
        }
    }

    @Override
    public void setData(List<Message> data) {

    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return null;
    }
}