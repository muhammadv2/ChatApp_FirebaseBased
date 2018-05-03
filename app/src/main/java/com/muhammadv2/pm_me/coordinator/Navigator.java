package com.muhammadv2.pm_me.coordinator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.ui.details.ChatDetailsActivity;

import java.util.Arrays;

import static com.muhammadv2.pm_me.Constants.CURRENT_USER_DATA;
import static com.muhammadv2.pm_me.Constants.RC_SIGN_IN;
import static com.muhammadv2.pm_me.Constants.TARGETED_USER_DATA;

public class Navigator {

    public void handleOpeningAuthSign(Activity context) {
        context.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.app_icon)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                                //Todo Here is the place to add more providers
                        ))
                        .build(),
                RC_SIGN_IN);
    }

    public void handleOnTargetUserClicked(Context context,
                                          String currentUserKey,
                                          String targetedUserKey) {
        Bundle bundle = new Bundle();
        bundle.putString(TARGETED_USER_DATA, targetedUserKey);
        bundle.putString(CURRENT_USER_DATA, currentUserKey);

        Intent intent = new Intent(context, ChatDetailsActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
