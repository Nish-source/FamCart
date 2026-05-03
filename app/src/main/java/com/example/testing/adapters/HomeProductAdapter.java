package com.example.testing.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.famcart.R;
import com.example.testing.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;
    private OnAddToCartListener cartListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    public HomeProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setOnAddToCartListener(OnAddToCartListener cartListener) {
        this.cartListener = cartListener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvName.setText(product.getName());
        holder.tvQuantity.setText(product.getQuantity());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", product.getPrice()));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.essentials)
                    .centerCrop()
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.essentials);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        // Task 4: "+" Button Functionality
        holder.btnPlus.setOnClickListener(v -> {
            if (cartListener != null) {
                cartListener.onAddToCart(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnPlus;
        TextView tvName, tvQuantity, tvPrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            btnPlus = itemView.findViewById(R.id.btn_add_to_cart_home);
        }
    }
}
