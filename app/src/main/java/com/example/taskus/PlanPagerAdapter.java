package com.example.taskus;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PlanPagerAdapter extends FragmentStateAdapter {

    //默认构造函数
    public PlanPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:return new DailyPlanFragment();
            case 1:return new WeeklyPlanFragment();
            case 2:return new MonthlyPlanFragment();
            case 3:return new PhasePlanFragment();
            default: throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
