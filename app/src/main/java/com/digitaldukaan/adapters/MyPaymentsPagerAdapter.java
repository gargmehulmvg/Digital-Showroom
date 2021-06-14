package com.digitaldukaan.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.digitaldukaan.fragments.SettlementsFragment;
import com.digitaldukaan.fragments.TransactionsFragment;

import org.jetbrains.annotations.NotNull;

public class MyPaymentsPagerAdapter extends FragmentStatePagerAdapter {

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
        if (position == 0) {
            return  "Transactions";
        } else {
            return  "Settlements";
        }
    }
}
