package com.muhammadv2.pm_me.main;


import android.content.Context;
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

import com.firebase.ui.auth.AuthUI;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.Utils.GlideImageHandlingUtils;
import com.muhammadv2.pm_me.coordinator.RootCoordinator;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UsersFragment
        extends MvpLceFragment<CoordinatorLayout, List<AuthUser>, IUsersView, UsersPresenterImp>
        implements IUsersView, UsersAdapter.OnItemClickLister {

    public static final int RC_SIGN_IN = 305;
    public static final String CURRENT_USER_DATA = "currentUid";
    public static final String TARGETED_USER_DATA = "chooseUid";

    @BindView(R.id.contentView)
    RecyclerView mUsersRV;
    UsersAdapter mUsersAdapter;
    @BindView(R.id.iv_current_user_img)
    ImageView mIvUserImage;
    @BindView(R.id.tv_current_user_name)
    TextView mTvUserName;

    private Context mContext = getContext();
    private RootCoordinator coordinator;

    private UsersPresenterImp usersPresenter = new UsersPresenterImp(this);

    public UsersFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public UsersPresenterImp createPresenter() {
        return usersPresenter;
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
        coordinator = new RootCoordinator();
        loadData(false);
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        presenter.loadDataIfUserAuthOrShowSignInScreen();
    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return null;
    }

    private void attachUI(List<AuthUser> authUsers) {
        mUsersRV.setHasFixedSize(true);
        mUsersRV.setLayoutManager(new LinearLayoutManager(mContext));
        mUsersAdapter = new UsersAdapter(authUsers, this);
        mUsersRV.setAdapter(mUsersAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setAuthStateListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.detachListeners();
    }

    @Override
    public void onClick(int position) {

//        coordinator.handleOnTargetUserClicked();
//        AuthUser currentUser = mAuthUsers.get(position);
//        Bundle bundle = new Bundle();
//
//        bundle.putParcelable(TARGETED_USER_DATA, currentUser);
//        bundle.putParcelable(CURRENT_USER_DATA, mCurrentUser);
//
//        Intent intent = new Intent(mActivity, ChatDetailsActivity.class);
//        intent.putExtras(bundle);
//        startActivity(intent);
    }

    @Override
    public void setData(List<AuthUser> data) {
        attachUI(data);
    }

    @Override
    public void showSignIn() {
        // Start the Firebase UI for logging in by email and google provider
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setLogo(R.drawable.app_icon)
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                                // You can add more providers here
                        ))
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void showCurrentUserInfo(String name, String imageUrl) {
        mTvUserName.setText(name);
        GlideImageHandlingUtils.loadImageIntoView(
                getActivity(),
                imageUrl,
                mIvUserImage);
    }
}
