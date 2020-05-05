package com.example.sis;

import android.print.PrintManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PlanPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    public User u;
    public PrintManager printManager;

    public PlanPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                PlanTabFragment1 tab1 = new PlanTabFragment1();
                tab1.user = u;
                return tab1;
            case 1:
                PlanTabFragment2 tab2 = new PlanTabFragment2();
                tab2.user = u;
                tab2.printManager = printManager;
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}