package com.muhammadv2.pm_me.users;

import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

abstract class UsersPresenter extends MvpBasePresenter<IUsersView> {

    public abstract void loadDataIfUserAuthOrShowSignInScreen();

    @UiThread
    public abstract void getChatUsers(int position);
}
