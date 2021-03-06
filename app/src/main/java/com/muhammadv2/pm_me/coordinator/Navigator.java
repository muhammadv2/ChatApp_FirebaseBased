package com.muhammadv2.pm_me.coordinator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.model.AuthUser;
import com.muhammadv2.pm_me.ui.details.ChatDetailsActivity;

import java.util.Arrays;

import static com.muhammadv2.pm_me.Constants.CURRENT_USER_DATA;
import static com.muhammadv2.pm_me.Constants.RC_SIGN_IN;
import static com.muhammadv2.pm_me.Constants.TARGETED_USER_DATA;

public class Navigator {

    //region UsersComponent Methods
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
                                          AuthUser currentUser,
                                          AuthUser targetedUser) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CURRENT_USER_DATA, currentUser);
        bundle.putParcelable(TARGETED_USER_DATA, targetedUser);

        Intent intent = new Intent(context, ChatDetailsActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    //endregion
}
