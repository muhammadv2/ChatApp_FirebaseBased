package com.muhammadv2.pm_me.ui.users;

import android.support.annotation.UiThread;

import com.hannesdorfmann.mosby3.mvp.MvpNullObjectBasePresenter;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.List;

import java9.util.function.Consumer;

abstract class IUserPresenter extends MvpNullObjectBasePresenter<IUsersView> {

    public abstract void loadData(Consumer<List<AuthUser>> onNewResult);

    @UiThread
    public abstract void onTargetUserClicked(int position);

    @UiThread
    public abstract void setAuthStateListener();

    @UiThread
    public abstract void detachListeners();
}
