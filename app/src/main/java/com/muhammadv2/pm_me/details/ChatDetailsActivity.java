package com.muhammadv2.pm_me.details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.main.AuthUser;
import com.muhammadv2.pm_me.main.UsersFragment;

import static com.muhammadv2.pm_me.details.ChatDetailsFragment.RC_PHOTO_PICKER;


public class ChatDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_details);

        ActionBar actionBar = getSupportActionBar();
        Bundle bundle = getIntent().getExtras();
        if (actionBar != null && bundle != null) {
            AuthUser authUser = bundle.getParcelable(UsersFragment.TARGETED_USER_DATA);
            if (authUser == null) return;
            actionBar.setTitle(authUser.getName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            ChatDetailsFragment chatDetailsFragment = new ChatDetailsFragment();
            chatDetailsFragment.handleSelectedImage(data);
        }

    }

}
