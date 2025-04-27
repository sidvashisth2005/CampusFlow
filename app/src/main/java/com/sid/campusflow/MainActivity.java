package com.sid.campusflow;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.sid.campusflow.adapters.ViewPagerAdapter;
import com.sid.campusflow.fragments.BookingFragment;
import com.sid.campusflow.fragments.CommunityFragment;
import com.sid.campusflow.fragments.ExploreFragment;
import com.sid.campusflow.fragments.HomeFragment;
import com.sid.campusflow.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        viewPager = findViewById(R.id.view_pager);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Setup ViewPager
        setupViewPager();
        
        // Setup Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(this);
        
        // Prevent swiping between fragments
        viewPager.setUserInputEnabled(false);
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new ExploreFragment());
        fragments.add(new BookingFragment());
        fragments.add(new CommunityFragment());
        fragments.add(new ProfileFragment());

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);
        viewPager.setAdapter(viewPagerAdapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.navigation_home);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.navigation_explore);
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.navigation_booking);
                        break;
                    case 3:
                        bottomNavigation.setSelectedItemId(R.id.navigation_community);
                        break;
                    case 4:
                        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
                        break;
                }
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_home) {
            viewPager.setCurrentItem(0, false);
            return true;
        } else if (itemId == R.id.navigation_explore) {
            viewPager.setCurrentItem(1, false);
            return true;
        } else if (itemId == R.id.navigation_booking) {
            viewPager.setCurrentItem(2, false);
            return true;
        } else if (itemId == R.id.navigation_community) {
            viewPager.setCurrentItem(3, false);
            return true;
        } else if (itemId == R.id.navigation_profile) {
            viewPager.setCurrentItem(4, false);
            return true;
        }
        
        return false;
    }
} 
