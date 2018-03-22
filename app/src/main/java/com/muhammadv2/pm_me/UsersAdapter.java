package com.muhammadv2.pm_me;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<AuthUser> mAuthUsers;

    public UsersAdapter(List<AuthUser> authUsers) {
        mAuthUsers = authUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        AuthUser user = mAuthUsers.get(position);

        holder.userName.setText(user.getName());


    }

    @Override
    public int getItemCount() {
        if (mAuthUsers == null) return 0;
        return mAuthUsers.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_username)
        TextView userName;
        @BindView(R.id.iv_user_image)
        ImageView userImage;

        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
