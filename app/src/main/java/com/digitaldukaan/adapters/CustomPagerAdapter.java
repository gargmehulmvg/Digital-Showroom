package com.digitaldukaan.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.digitaldukaan.R;
import com.digitaldukaan.constants.StaticInstances;

public class CustomPagerAdapter extends PagerAdapter {

    private final LayoutInflater inflater;
    private Context mContext = null;

    public CustomPagerAdapter(Context context) {
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return StaticInstances.INSTANCE.getSHelpScreenList().size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.layout_help_screen_item, view, false);
        final ImageView imageView = imageLayout.findViewById(R.id.image);
        if (imageView != null) {
            try {
                if (mContext != null)
                    Glide.with(mContext).load(StaticInstances.INSTANCE.getSHelpScreenList().get(position).getUrl()).into(imageView);
            } catch (Exception e) {
                Log.e(CustomPagerAdapter.class.getSimpleName(), "picasso image loading issue: ${e.message}", e);
            }
        }
        view.addView(imageLayout, 0);
        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}