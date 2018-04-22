package com.muhammadv2.pm_me.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.muhammadv2.pm_me.R;
import com.muhammadv2.pm_me.model.Message;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessagesList;

    MessageAdapter(List<Message> messagesList) {
        mMessagesList = messagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Message message = mMessagesList.get(position);
        holder.senderName.setText(message.getSenderName());
        holder.messageBody.setText(message.getMessageBody());

        Glide.with(holder.messageImage.getContext())
                .load(message.getImageUrl())
                .into(holder.messageImage);
    }

    @Override
    public int getItemCount() {
        if (mMessagesList == null) return 0;
        return mMessagesList.size();
    }

    public void clear() {
        if (mMessagesList == null) return;
        final int size = mMessagesList.size();
        mMessagesList.clear();
        notifyItemRangeRemoved(0, size);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.nameTextView)
        TextView senderName;
        @BindView(R.id.messageTextView)
        TextView messageBody;
        @BindView(R.id.photoImageView)
        ImageView messageImage;

        MessageViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
