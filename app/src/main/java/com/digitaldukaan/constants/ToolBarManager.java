package com.digitaldukaan.constants;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.digitaldukaan.MainActivity;
import com.digitaldukaan.R;
import com.digitaldukaan.interfaces.IOnToolbarIconClick;

public class ToolBarManager {

    private static final ToolBarManager ourInstance = new ToolBarManager();
    private Toolbar mToolbar;

    public static ToolBarManager getInstance() {
        return ourInstance;
    }

    private ToolBarManager() {
    }

    public void setHeaderTitle(String title) {
        TextView v = mToolbar.findViewById(R.id.appTitleTextView);
        v.setText(title);
    }
    
    public void setSideIcon(Drawable drawable, IOnToolbarIconClick listener) {
        ImageView imageView = mToolbar.findViewById(R.id.sideIconToolbar);
        imageView.setImageDrawable(drawable);
        if (null != listener) {
            imageView.setOnClickListener(v -> listener.onToolbarSideIconClicked());
        }
    }

    public void setSideIconVisibility(boolean isVisible) {
        mToolbar.findViewById(R.id.sideIconToolbar).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setupToolbar(Toolbar mToolbar) {
        this.mToolbar = mToolbar;
    }

    public void onBackPressed(final Fragment fragment) {
        ImageView backImage = mToolbar.findViewById(R.id.backButtonToolbar);
        backImage.setOnClickListener(view -> {
            if (fragment.getActivity() != null) {
                fragment.getActivity().onBackPressed();
            }
        });
    }

    public void hideToolBar(MainActivity mActivity, boolean toHide) {
        if (toHide) {
            mActivity.findViewById(R.id.toolbarLayout).setVisibility(View.GONE);
        } else {
            mActivity.findViewById(R.id.toolbarLayout).setVisibility(View.VISIBLE);
        }
    }

    public void hideBackPressFromToolBar(MainActivity mActivity, boolean toHide) {
        if (toHide) {
            mActivity.findViewById(R.id.backButtonToolbar).setVisibility(View.INVISIBLE);
        } else {
            mActivity.findViewById(R.id.backButtonToolbar).setVisibility(View.VISIBLE);
        }
    }
}