package com.muhammadv2.pm_me.component.users;

import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;

abstract class UsersPresenter extends MvpNullObjectBasePresenter<IUsersView> {

    public abstract void loadDataIfUserAuthOrShowSignInScreen();

    @UiThread
    public abstract void getChatUsers(int position);

    @UiThread
    public abstract void setAuthStateListener();

    @UiThread
    public abstract void detachListeners();
}
