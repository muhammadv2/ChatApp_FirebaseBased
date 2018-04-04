package com.muhammadv2.pm_me.main;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.Utils.GlideImageHandlingUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<AuthUser> mAuthUsers;

    private OnItemClickLister mOnItemClickLister;

    @FunctionalInterface
    interface OnItemClickLister {
        void onClick(int position);
    }

    UsersAdapter(List<AuthUser> authUsers, OnItemClickLister onItemClickLister) {
        mAuthUsers = authUsers;
        mOnItemClickLister = onItemClickLister;
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

        GlideImageHandlingUtils.loadImageIntoView(
                holder.userImage.getContext(),
                user.getImageUrl(),
                holder.userImage);
        holder.userName.setText(user.getName());
    }

    @Override
    public int getItemCount() {
        if (mAuthUsers == null) return 0;
        return mAuthUsers.size();
    }

    public void clear() {
        if (mAuthUsers == null) return;
        final int size = mAuthUsers.size();
        mAuthUsers.clear();
        notifyItemRangeRemoved(0, size);
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_user_name)
        TextView userName;
        @BindView(R.id.iv_user_image)
        ImageView userImage;

        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnItemClickLister.onClick(getAdapterPosition());
        }
    }
}
