package com.muhammadv2.pm_me.coordinator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.muhammadv2.pm_me.component.details.ChatDetailsActivity;
import com.muhammadv2.pm_me.model.AuthUser;

import static com.muhammadv2.pm_me.Constants.CURRENT_USER_DATA;
import static com.muhammadv2.pm_me.Constants.TARGETED_USER_DATA;

public class RootCoordinator {

    public void handleOnTargetUserClicked(Context context,
                                          AuthUser currentUser,
                                          AuthUser targetedUser) {
        Bundle bundle = new Bundle();

        bundle.putParcelable(TARGETED_USER_DATA, targetedUser);
        bundle.putParcelable(CURRENT_USER_DATA, currentUser);

        Intent intent = new Intent(context, ChatDetailsActivity.class);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
