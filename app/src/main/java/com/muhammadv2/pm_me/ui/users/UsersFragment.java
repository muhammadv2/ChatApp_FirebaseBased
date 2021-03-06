package com.muhammadv2.pm_me.ui.users;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.Utils.GlideImageHandlingUtils;
import com.muhammadv2.pm_me.coordinator.Navigator;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UsersFragment
        extends MvpLceFragment<CoordinatorLayout, List<AuthUser>, IUsersView, UsersPresenter>
        implements IUsersView, UsersAdapter.OnItemClickLister {

    @BindView(R.id.contentView)
    RecyclerView mUsersRV;
    UsersAdapter mUsersAdapter;
    @BindView(R.id.iv_current_user_img)
    ImageView mIvUserImage;
    @BindView(R.id.tv_current_user_name)
    TextView mTvUserName;

    private Navigator navigator;

    public UsersFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public UsersPresenter createPresenter() {
        return new UsersPresenter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        navigator = new Navigator();
        loadData(false);
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        presenter.loadData(this::setData);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.attachView(this);
        presenter.setAuthStateListener();
    }

    @Override
    public void onClick(int position) {
        presenter.onTargetUserClicked(position);
    }

    @Override
    public void navigateChatDetails(AuthUser currentUser, AuthUser targetUser) {
        navigator.handleOnTargetUserClicked(getContext(), currentUser, targetUser);
    }

    @Override
    public void setData(List<AuthUser> authUsers) {
        mUsersRV.setHasFixedSize(true);
        mUsersRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsersAdapter = new UsersAdapter(authUsers, this);
        mUsersRV.setAdapter(mUsersAdapter);
    }

    @Override
    public void showSignIn() {
        // Start the Firebase UI for logging in by email and google provider
        navigator.handleOpeningAuthSign(getActivity());
    }

    @Override
    public void showCurrentUserInfo(String name, String imageUrl) {
        mTvUserName.setText(name);
        GlideImageHandlingUtils.loadImageIntoView(
                getActivity(),
                imageUrl,
                mIvUserImage);
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.detachListeners();
        presenter.detachView();
    }

    public void clearAdapter() {
        if (mUsersAdapter != null)
            mUsersAdapter.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.destroy();
    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return null;
    }
}
