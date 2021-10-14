package com.digitaldukaan.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.digitaldukaan.R;
import com.digitaldukaan.constants.StaticInstances;
import com.digitaldukaan.models.response.HelpScreenItemResponse;

public class LoginHelpPageAdapter extends PagerAdapter {

    private final LayoutInflater inflater;
    private Context mContext;

    public LoginHelpPageAdapter(Context context) {
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
        if (mContext != null) {
            final ImageView imageView = imageLayout.findViewById(R.id.image);
            final TextView headingOneTextView = imageLayout.findViewById(R.id.headingOneTextView);
            final TextView headingTwoTextView = imageLayout.findViewById(R.id.headingTwoTextView);
            final TextView headingThreeTextView = imageLayout.findViewById(R.id.headingThreeTextView);
            final HelpScreenItemResponse helpScreenItemResponse = StaticInstances.INSTANCE.getSHelpScreenList().get(position);
            headingOneTextView.setText(helpScreenItemResponse.getHeading1());
            headingTwoTextView.setText(helpScreenItemResponse.getHeading2());
            headingThreeTextView.setText(helpScreenItemResponse.getHeading3());
            if (0 == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_help_screen_one));
            } else {
                Glide.with(mContext).load(helpScreenItemResponse.getUrl()).into(imageView);
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