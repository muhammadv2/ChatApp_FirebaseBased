package com.muhammadv2.pm_me.ui.users;

import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby3.mvp.lce.MvpLceView;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.List;

interface IUsersView extends MvpLceView<List<AuthUser>> {

    @UiThread
    void showSignIn();

    @UiThread
    void showCurrentUserInfo(String name, String imageUrl);

    @UiThread
    void navigateChatDetails(String currentUser, String targetedUser);

    @UiThread
    void clearAdapter();
}
