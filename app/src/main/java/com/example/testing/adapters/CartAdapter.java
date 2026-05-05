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
import com.example.testing.models.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newCount);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(CartItemListener listener) {
        this.listener = listener;
    }

    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText(item.getProductQuantity());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", item.getTotalPrice()));
        holder.tvQtyCount.setText(String.valueOf(item.getCount()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.essentials)
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setImageResource(R.drawable.essentials);
        }

        holder.btnPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuantityChanged(item, item.getCount() + 1);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (listener != null) {
                if (item.getCount() > 1) {
                    listener.onQuantityChanged(item, item.getCount() - 1);
                } else {
                    listener.onItemRemoved(item);
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, btnPlus, btnMinus;
        View btnDelete; // FrameLayout in XML — must be View, not ImageView
        TextView tvName, tvQuantity, tvPrice, tvQtyCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvQtyCount = itemView.findViewById(R.id.tv_qty_count);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnPlus = itemView.findViewById(R.id.btn_qty_plus);
            btnMinus = itemView.findViewById(R.id.btn_qty_minus);
        }
    }
}
