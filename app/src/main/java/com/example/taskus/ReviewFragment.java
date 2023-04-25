package com.example.taskus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ReviewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_review,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //设置tabItems
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        TabLayout.Tab weeklyTab = tabLayout.newTab();
        weeklyTab.setText("周复盘");
        tabLayout.addTab(weeklyTab);

        TabLayout.Tab monthlyTab = tabLayout.newTab();
        monthlyTab.setText("月复盘");
        tabLayout.addTab(monthlyTab);


        //设置ViewPager的适配器
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        ReviewPagerAdapter reviewPagerAdapter = new ReviewPagerAdapter(this.requireActivity());
        viewPager.setAdapter(reviewPagerAdapter);

        //将其与TabLayout关联.
        //TabLayoutMediator是用于连接它们的辅助类。当用户点击TabLayout中的选项卡时，ViewPager2会切换到相应的页面。反之亦然，当用户在ViewPager2中滑动页面时，TabLayout中的选项卡也会发生改变。
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout,viewPager,(tab, position)->{
            switch (position){
                case 0:
                    tab.setText("周复盘");
                    break;
                case 1:
                    tab.setText("月复盘");
                    break;
            }
        });
        tabLayoutMediator.attach();

    }
}