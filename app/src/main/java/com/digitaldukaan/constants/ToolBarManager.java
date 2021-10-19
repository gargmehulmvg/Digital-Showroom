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
import com.digitaldukaan.interfaces.IOnToolbarSecondIconClick;

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
        TextView subHeadingView = mToolbar.findViewById(R.id.appSubTitleTextView);
        subHeadingView.setVisibility(View.GONE);
        v.setText(title);
    }

    public String getHeaderTitle() {
        TextView view = mToolbar.findViewById(R.id.appTitleTextView);
        return view.getText().toString();
    }

    public void setHeaderSubTitle(String title) {
        TextView v = mToolbar.findViewById(R.id.appSubTitleTextView);
        v.setVisibility(View.VISIBLE);
        v.setText(title);
    }

    public void setSideIcon(Drawable drawable, IOnToolbarIconClick listener) {
        ImageView imageView = mToolbar.findViewById(R.id.sideIconToolbar);
        imageView.setImageDrawable(drawable);
        if (null != listener) { imageView.setOnClickListener(v -> listener.onToolbarSideIconClicked()); }
    }

    public void setSecondSideIcon(Drawable drawable, IOnToolbarSecondIconClick listener) {
        ImageView imageView = mToolbar.findViewById(R.id.sideIcon2Toolbar);
        imageView.setImageDrawable(drawable);
        if (null != listener) {
            imageView.setOnClickListener(v -> listener.onToolbarSecondIconClicked());
        }
    }

    public void setSideIconVisibility(boolean isVisible) {
        mToolbar.findViewById(R.id.sideIconToolbar).setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setSecondSideIconVisibility(boolean isVisible) {
        mToolbar.findViewById(R.id.sideIcon2Toolbar).setVisibility(isVisible ? View.VISIBLE : View.GONE);
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

    public void hideToolBar(MainActivity activity, boolean toHide) {
        if (activity == null) return;
        if (toHide) {
            activity.findViewById(R.id.toolbarLayout).setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.toolbarLayout).setVisibility(View.VISIBLE);
        }
    }

    public void hideBackPressFromToolBar(MainActivity activity, boolean toHide) {
        if (activity == null) return;
        if (toHide) {
            activity.findViewById(R.id.backButtonToolbar).setVisibility(View.GONE);
        } else {
            activity.findViewById(R.id.backButtonToolbar).setVisibility(View.VISIBLE);
        }
    }
}