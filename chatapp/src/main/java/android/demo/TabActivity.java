package android.demo;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.chat21.android.core.ChatManager;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.contacts.fragments.ContactsListFragment;

import java.util.HashMap;
import java.util.Map;

import chat21.android.demo.R;


public class TabActivity extends AppCompatActivity {

    ChatFragment chatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chatFragment = new ChatFragment();
        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.listcontainer, chatFragment)
                .commit();
//         #### BEGIN CONTAINER ####

        ChatUI.getInstance().processRemoteNotification(getIntent());
    }

    @Override
    protected void onResume() {
        ChatManager.getInstance().getMyPresenceHandler().connect();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        ChatManager.getInstance().getMyPresenceHandler().dispose();
        super.onDestroy();
    }
}