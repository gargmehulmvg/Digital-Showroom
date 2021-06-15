package com.digitaldukaan.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.digitaldukaan.fragments.BaseFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MyPaymentsPagerAdapter extends FragmentStatePagerAdapter {

    private final ArrayList<BaseFragment> mFragmentList;
    private final ArrayList<String> mFragmentHeaderList;

    public MyPaymentsPagerAdapter(FragmentManager fm, ArrayList<BaseFragment> list, ArrayList<String> headerNameList) {
        super(fm);
        this.mFragmentList = list;
        this.mFragmentHeaderList = headerNameList;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentHeaderList.get(position);
    }
}
