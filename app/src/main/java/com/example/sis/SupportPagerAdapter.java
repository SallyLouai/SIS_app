package com.example.sis;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class SupportPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    SupportPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                SupportTabFragment1 tab1 = new SupportTabFragment1();
                return tab1;
            case 1:
                SupportTabFragment2 tab2 = new SupportTabFragment2();
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