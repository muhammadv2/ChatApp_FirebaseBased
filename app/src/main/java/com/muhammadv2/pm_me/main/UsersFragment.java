package com.muhammadv2.pm_me.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.database.DataSnapshot;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceFragment;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.Utils.GlideImageHandlingUtils;
import com.muhammadv2.pm_me.details.ChatDetailsActivity;
import com.muhammadv2.pm_me.model.AuthUser;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This class is to show a list of authenticated users by firstly fire
 * {@link #mAuthListener} to find if the current user is registered if not redirect him to sign up
 * screen if yes then load the list of the users by calling this method {@link #loadAllAuthUsers()}
 * and also make sure to add the current user  {@link #addAllUserToTheList(DataSnapshot)}
 */
public class UsersFragment
        extends MvpLceFragment<SwipeRefreshLayout, List<AuthUser>, IUsersView, UsersPresenterImp>
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
    private Activity mActivity;

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
        if (getActivity() != null) mActivity = getActivity();
        ButterKnife.bind(this, view);
        setRetainInstance(true);
        loadData(false);
    }

    @Override
    public void loadData(boolean pullToRefresh) {
        presenter.loadData();
    }

    @Override
    protected String getErrorMessage(Throwable e, boolean pullToRefresh) {
        return null;
    }

    private void initAndPopulateRv(List<AuthUser> authUsers) {
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
        AuthUser currentUser = mAuthUsers.get(position);
        Bundle bundle = new Bundle();

        bundle.putParcelable(TARGETED_USER_DATA, currentUser);
        bundle.putParcelable(CURRENT_USER_DATA, mCurrentUser);

        Intent intent = new Intent(mActivity, ChatDetailsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public static void signUserOut(Context context) {
        AuthUI.getInstance().signOut(context); // Sign out function as simple as that
    }

    @Override
    public void setData(List<AuthUser> data) {
        initAndPopulateRv(data);

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
    public void showCurrentUserInfo() {
        mTvUserName.setText(mCurrentUser.getName());
        GlideImageHandlingUtils.loadImageIntoView(
                mActivity,
                mCurrentUser.getImageUrl(),
                mIvUserImage);
    }

}
