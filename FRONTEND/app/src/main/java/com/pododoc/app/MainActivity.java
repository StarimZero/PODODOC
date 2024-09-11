package com.pododoc.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Fragment> fragments;
    private TabLayout tab;
    private ViewPager pager;
    TextView userEmail;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    Button logoutButton;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize fragments
        fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new SearchFragment());
        fragments.add(new WishListFragment());
        fragments.add(new MypageFragment());

        // Initialize TabLayout and ViewPager
        tab = findViewById(R.id.tab);
        pager = findViewById(R.id.pager);

        userEmail = findViewById(R.id.userEmail);
        String email = user.getEmail();
        String userName = email.split("@")[0];
        userEmail.setText(userName);

        logoutButton = findViewById(R.id.logout);
        drawerLayout = findViewById(R.id.main);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MainActivity", "Logout button clicked");
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setupTabs();
        setupViewPager();

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // No action needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // No action needed
            }
        });

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Update title based on current fragment
                updateTitle(position);
            }
        });

        // Set initial title
        updateTitle(0);

        // Set Activity title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupTabs() {
        tab.addTab(tab.newTab().setIcon(R.drawable.star));
        tab.addTab(tab.newTab().setIcon(R.drawable.search));
        tab.addTab(tab.newTab().setIcon(R.drawable.wishlist));
        tab.addTab(tab.newTab().setIcon(R.drawable.mypage));
    }

    private void setupViewPager() {
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pager.setAdapter(adapter);
    }

    private void updateTitle(int position) {
        String title = "";
        switch (position) {
            case 0:
                title = "당신을 위한 와인추천";
                break;
            case 1:
                title = "와인찾기";
                break;
            case 2:
                title = "즐겨찾기 리스트";
                break;
            case 3:
                title = "내정보";
                break;
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }else{
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}