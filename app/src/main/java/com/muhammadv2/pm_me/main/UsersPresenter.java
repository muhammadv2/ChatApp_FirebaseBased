package com.muhammadv2.pm_me.main;

import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;

abstract class UsersPresenter extends MvpBasePresenter<IUsersView> {

    @UiThread
    public abstract void loadDataIfUserAuthOrShowSignInScreen();

}
