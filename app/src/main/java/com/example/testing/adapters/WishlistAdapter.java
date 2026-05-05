package com.example.testing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
import com.bumptech.glide.Glide;
=======
>>>>>>> 5ca5e1075dd70c549c30ca34e25cc36adec93a17
import com.example.famcart.R;
import com.example.testing.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Product> wishlistItems = new ArrayList<>();
    private WishlistListener listener;

    public interface WishlistListener {
        void onRemoveFromWishlist(Product product);
        void onAddToCart(Product product);
        void onProductClick(Product product);
    }

    public WishlistAdapter(WishlistListener listener) {
        this.listener = listener;
    }

    public void updateItems(List<Product> items) {
        this.wishlistItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product item = wishlistItems.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(item.getQuantity());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", item.getPrice()));

<<<<<<< HEAD
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.essentials)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.essentials);
=======
        if (item.getDrawableResId() != 0) {
            holder.ivImage.setImageResource(item.getDrawableResId());
>>>>>>> 5ca5e1075dd70c549c30ca34e25cc36adec93a17
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveFromWishlist(item);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return wishlistItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnRemove;
        TextView tvName, tvQuantity, tvPrice, btnAddToCart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}
