package com.muhammadv2.pm_me.Utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.muhammadv2.pm_me.R;

public class GlideImageHandlingUtils {
    public static void loadImageIntoView(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .applyDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.man))
                .load(url)
                .into(imageView);
    }
}
