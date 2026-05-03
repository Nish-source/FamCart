
package com.example.testing.utils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.famcart.R;

public class ImageLoader{
    private static final String TAG = "ImageLoader";

    public static void load(Context context, String imageUrl, int drawableResId, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Log.d(TAG, "Loading URL: " + imageUrl);
            Glide.with(context)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_launcher_foreground) // shown while loading
                            .error(R.drawable.ic_launcher_foreground)        // shown on failure
                            .diskCacheStrategy(DiskCacheStrategy.ALL))        // cache image
                    .into(imageView);
        } else if (drawableResId != 0) {

            imageView.setImageResource(drawableResId);
        } else {

            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    public static void loadProduct(Context context,
                                   com.example.testing.models.Product product,
                                   ImageView imageView) {
        load(context, product.getImageUrl(), product.getDrawableResId(), imageView);
    }

    public static void loadCartItem(Context context,
                                    com.example.testing.models.CartItem item,
                                    ImageView imageView) {
        load(context, item.getImageUrl(), item.getDrawableResId(), imageView);
    }
}
