package org.chat21.android.ui.conversations.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.chat21.android.ui.conversations.fragments.ConversationListFragment;

import chat21.android.demo.R;

/**
 * Created by stefano on 15/10/2016.
 */
public class ConversationListActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conversation_list);

        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
//            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        }

        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new ConversationListFragment())
                .commit();
        // #### BEGIN CONTAINER ####
    }
}