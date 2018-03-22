package com.muhammadv2.pm_me;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;

public class ChatActivity extends AppCompatActivity {

    @BindView(R.id.rv_users)
    RecyclerView mUsersRV;
    UsersAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}
