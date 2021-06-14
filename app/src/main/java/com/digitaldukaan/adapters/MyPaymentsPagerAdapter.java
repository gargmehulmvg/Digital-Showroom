package com.digitaldukaan.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.digitaldukaan.fragments.SettlementsFragment;
import com.digitaldukaan.fragments.TransactionsFragment;

import org.jetbrains.annotations.NotNull;

public class MyPaymentsPagerAdapter extends FragmentPagerAdapter {

    public MyPaymentsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new TransactionsFragment();
        } else {
            fragment = new SettlementsFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title;
        if (position == 0) {
            title = "Transactions";
        } else {
            title = "Settlements";
        }
        return title;
    }
}
