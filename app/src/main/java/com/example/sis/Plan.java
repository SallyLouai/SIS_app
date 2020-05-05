package com.example.sis;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.print.PrintManager;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sis.Network.NetworkUtils;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

public class Plan extends AppCompatActivity {
    private SharedPreferences pref;
    private NetworkUtils networkUtils;
    public User user;

    private ListView plan_lstview;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        pref = getSharedPreferences("user_details",MODE_PRIVATE);
        user = new User(pref.getString("UserLogin", null ) );

        // display user information in action bar
        ActionBar actionBar = this.getSupportActionBar();
        String classTitle = actionBar.getTitle().toString();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.custom_action_bar);
        View view = actionBar.getCustomView();
        TextView activity_title = view.findViewById(R.id.activity_title);
        activity_title.setText(classTitle);
        TextView user_id_name = view.findViewById(R.id.user_id_name);
        user_id_name.setText( user.getStdCode() + "  |  " + user.getStdArName());

        TabLayout tabLayout = findViewById(R.id.plan_tab_layout);

        final ViewPager viewPager = findViewById(R.id.plan_pager);
        final PlanPagerAdapter adapter = new PlanPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());

        adapter.u = user;
        adapter.printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void onOptionsItemSelected(View v){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        finish();
    }
}
