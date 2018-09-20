package com.example.rahul.chatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private Toolbar main_page_toolbar;
    private ViewPager main_view_pager;
    private TabLayout main_tabs;
    private SectionPagerAdapter mSectionPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        main_page_toolbar= (Toolbar) findViewById(R.id.main_page_toolbar);
        main_tabs= (TabLayout) findViewById(R.id.main_tabs);
        setSupportActionBar(main_page_toolbar);
        getSupportActionBar().setTitle("HeyChat");

        main_view_pager= (ViewPager) findViewById(R.id.main_view_pager);
        mSectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        main_view_pager.setAdapter(mSectionPagerAdapter);
        main_tabs.setupWithViewPager(main_view_pager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if user signed in or not
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null)
        {
            sendToStart();
        }
    }
    private void sendToStart()
    {
        Intent startIntent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                sendToStart();
                break;
            case R.id.account_setting:
                Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.all_users:
                Intent userIntent=new Intent(MainActivity.this,UsersActivitiy.class);
                startActivity(userIntent);
                break;
        }
        super.onOptionsItemSelected(item);
        return true;
    }
}
