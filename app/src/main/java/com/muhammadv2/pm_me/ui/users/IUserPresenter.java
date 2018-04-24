package com.muhammadv2.pm_me.ui.users;

import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;

abstract class IUserPresenter extends MvpNullObjectBasePresenter<IUsersView> {

    public abstract void loadDataIfUserAuthOrShowSignScreen();

    @UiThread
    public abstract void getChatUsers(int position);

    @UiThread
    public abstract void setAuthStateListener();

    @UiThread
    public abstract void detachListeners();
}
