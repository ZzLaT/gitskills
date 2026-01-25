package com.example.personalaccounting.controller;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.personalaccounting.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String KEY_CURRENT_TAB = "current_tab";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FragmentAdapter fragmentAdapter;

    private static final int[] TAB_ICONS = {
            R.drawable.ic_home_inactive,
            R.drawable.ic_calendar_inactive,
            R.drawable.ic_statistics_inactive
    };

    private static final int[] TAB_ICONS_ACTIVE = {
            R.drawable.ic_home_active,
            R.drawable.ic_calendar_active,
            R.drawable.ic_statistics_active
    };

    private static final String[] TAB_TITLES = {"首页", "日历", "统计"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewPager();
        setupTabLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null) {
            outState.putInt(KEY_CURRENT_TAB, viewPager.getCurrentItem());
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(KEY_CURRENT_TAB) && viewPager != null) {
            int currentTab = savedInstanceState.getInt(KEY_CURRENT_TAB, 0);
            viewPager.setCurrentItem(currentTab, false);
        }
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void setupViewPager() {
        fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(TAB_TITLES[position]);
            tab.setIcon(TAB_ICONS[position]);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tab.setIcon(TAB_ICONS_ACTIVE[position]);
                Log.d(TAG, "Tab selected: " + TAB_TITLES[position]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tab.setIcon(TAB_ICONS[position]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.d(TAG, "Tab reselected: " + TAB_TITLES[position]);
                refreshFragmentAt(position);
            }
        });
    }

    private void refreshCurrentFragment() {
        int currentPosition = viewPager.getCurrentItem();
        refreshFragmentAt(currentPosition);
    }

    private void refreshFragmentAt(int position) {
        if (fragmentAdapter == null) {
            return;
        }

        androidx.fragment.app.Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag("f" + position);

        if (fragment != null && fragment.isAdded()) {
            switch (position) {
                case 0:
                    if (fragment instanceof HomeFragment) {
                        ((HomeFragment) fragment).refreshData();
                    }
                    break;
                case 1:
                    if (fragment instanceof CalendarFragment) {
                        ((CalendarFragment) fragment).refreshData();
                    }
                    break;
                case 2:
                    if (fragment instanceof StatisticsFragment) {
                        ((StatisticsFragment) fragment).refreshData();
                    }
                    break;
            }
        }
    }

    private void cleanup() {
        tabLayout = null;
        viewPager = null;
        fragmentAdapter = null;
    }
}
