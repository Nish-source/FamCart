package com.example.testing.adapters;

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

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;
    private OnOfferActionListener actionListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnOfferActionListener {
        void onAddToCart(Product product);
        void onOrderNow(Product product);
    }

    public OfferAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void setOnOfferActionListener(OnOfferActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offer_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvName.setText(product.getName());
        holder.tvDescription.setText(product.getDescription());
        
        double discount = 0;
        if (product.getOriginalPrice() > product.getPrice()) {
            discount = ((product.getOriginalPrice() - product.getPrice()) / product.getOriginalPrice()) * 100;
        }
        
        if (discount > 0) {
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(String.format(Locale.getDefault(), "%.0f%% OFF", discount));
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

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
        
        // Task 4: "+" Button Functionality (Offer)
        holder.btnAddToCart.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAddToCart(product);
            }
        });

        // Task 3: "Order Now" Button Functionality
        holder.btnOrderNow.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onOrderNow(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnAddToCart;
        TextView tvName, tvDescription, tvDiscount, btnOrderNow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_offer_image);
            tvName = itemView.findViewById(R.id.tv_offer_name);
            tvDescription = itemView.findViewById(R.id.tv_offer_description);
            tvDiscount = itemView.findViewById(R.id.tv_offer_discount);
            btnAddToCart = itemView.findViewById(R.id.btn_offer_add_to_cart);
            btnOrderNow = itemView.findViewById(R.id.btn_offer_order_now);
        }
    }
}
