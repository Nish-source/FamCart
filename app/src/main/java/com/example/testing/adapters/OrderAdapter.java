package com.example.testing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.models.CartItem;
import com.example.testing.models.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orders = new ArrayList<>();

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        // Order ID (shortened)
        String shortId = order.getOrderId();
        if (shortId != null && shortId.length() > 8) {
            shortId = shortId.substring(0, 8).toUpperCase();
        }
        holder.tvOrderId.setText("Order #" + shortId);

        // Status
        holder.tvStatus.setText(order.getStatus() != null ? order.getStatus() : "Placed");

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(order.getTimestamp())));

        // Total
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.0f", order.getTotalAmount()));

        // Build order items list
        holder.layoutItems.removeAllViews();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                View itemRow = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(android.R.layout.simple_list_item_1, holder.layoutItems, false);
                TextView tv = itemRow.findViewById(android.R.id.text1);
                tv.setTextSize(13);
                tv.setTextColor(0xFF6A7282);
                tv.setText(String.format(Locale.getDefault(),
                        "%s × %d — ₹%.0f",
                        item.getProductName(),
                        item.getCount(),
                        item.getTotalPrice()));
                holder.layoutItems.addView(itemRow);
            }
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvDate, tvTotal;
        LinearLayout layoutItems;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            layoutItems = itemView.findViewById(R.id.layout_order_items);
        }
    }
}