package com.pododoc.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    ArrayList<Fragment> fragments=new ArrayList<Fragment>();
    DrawerLayout drawerLayout;
    LinearLayout drawerView;
    TabLayout tab;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle("Pick for you");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fragments.add(new HomeFragment());
        fragments.add(new SearchFragment());
        fragments.add(new WishList());
        fragments.add(new MypageFragment());


        TabLayout tab = findViewById(R.id.tab);
        tab.addTab(tab.newTab().setText(""));
        tab.getTabAt(0).setIcon(R.drawable.star);

        tab.addTab(tab.newTab().setText(""));
        tab.getTabAt(1).setIcon(R.drawable.search);

        tab.addTab(tab.newTab().setText(""));
        tab.getTabAt(2).setIcon(R.drawable.wishlist);

        tab.addTab(tab.newTab().setText(""));
        tab.getTabAt(3).setIcon(R.drawable.mypage);

        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        ViewPager pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
    }//oncreate


    class PagerAdapter extends FragmentPagerAdapter{

        public PagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}//activitiy