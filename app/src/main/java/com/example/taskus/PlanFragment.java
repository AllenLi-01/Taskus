/*
 * Copyright (c) 2023 Allen Li
 *
 * All rights reserved.
 *
 * This software is the property of Allen Li and is protected by copyright,
 * trademark, and other intellectual property laws. You may not reproduce, modify,
 * distribute, or create derivative works based on this software, in whole or in part,
 * without the express written permission of Allen Li.
 */

package com.example.taskus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class PlanFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_plan,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //设置tabItems
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        TabLayout.Tab dailyTab = tabLayout.newTab();
        dailyTab.setText(R.string.daily);
        tabLayout.addTab(dailyTab);

        TabLayout.Tab weeklyTab = tabLayout.newTab();
        weeklyTab.setText(R.string.weekly);
        tabLayout.addTab(weeklyTab);

        TabLayout.Tab monthlyTab = tabLayout.newTab();
        monthlyTab.setText(R.string.monthly);
        tabLayout.addTab(monthlyTab);

        TabLayout.Tab phaseTab = tabLayout.newTab();
        phaseTab.setText(R.string.phase);
        tabLayout.addTab(phaseTab);

        //设置ViewPager的适配器
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        PlanPagerAdapter planPagerAdapter = new PlanPagerAdapter(this.requireActivity());
        viewPager.setAdapter(planPagerAdapter);

        //将其与TabLayout关联.
        //TabLayoutMediator是用于连接它们的辅助类。当用户点击TabLayout中的选项卡时，ViewPager2会切换到相应的页面。反之亦然，当用户在ViewPager2中滑动页面时，TabLayout中的选项卡也会发生改变。
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout,viewPager,(tab, position)->{
            switch (position){
                case 0:
                    tab.setText(R.string.daily);
                    break;
                case 1:
                    tab.setText(R.string.weekly);
                    break;
                case 2:
                    tab.setText(R.string.monthly);
                    break;
                case 3:
                    tab.setText(R.string.phase);
                    break;
            }
        });
        tabLayoutMediator.attach();
    }
}
