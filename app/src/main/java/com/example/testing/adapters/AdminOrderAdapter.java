package com.example.testing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.models.AdminOrder;
import com.example.testing.models.CartItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    public interface OnOrderActionListener {
        void onAccept(AdminOrder order, int position);
        void onReject(AdminOrder order, int position);
        void onComplete(AdminOrder order, int position);
        void onOrderClick(AdminOrder order);
    }

    private List<AdminOrder> orders = new ArrayList<>();
    private OnOrderActionListener listener;

    public AdminOrderAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void updateOrders(List<AdminOrder> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminOrder order = orders.get(position);

        // Order ID (shortened)
        String shortId = order.getOrderId();
        if (shortId != null && shortId.length() > 8) {
            shortId = shortId.substring(0, 8).toUpperCase();
        }
        holder.tvOrderId.setText("Order #" + shortId);

        // Customer name
        String userName = order.getUserName();
        if (userName != null && !userName.isEmpty()) {
            holder.tvUserName.setText("👤 " + userName);
            holder.tvUserName.setVisibility(View.VISIBLE);
        } else {
            // Fallback to shortened userId
            String uid = order.getUserId();
            if (uid != null && uid.length() > 12) {
                uid = uid.substring(0, 12) + "…";
            }
            holder.tvUserName.setText("User: " + (uid != null ? uid : "Unknown"));
            holder.tvUserName.setVisibility(View.VISIBLE);
        }

        // Address
        String address = order.getUserAddress();
        if (address != null && !address.isEmpty()) {
            holder.tvUserAddress.setText("📍 " + address);
            holder.tvUserAddress.setVisibility(View.VISIBLE);
        } else {
            holder.tvUserAddress.setVisibility(View.GONE);
        }

        // Phone
        String phone = order.getUserPhone();
        if (phone != null && !phone.isEmpty()) {
            holder.tvUserPhone.setText("📞 " + phone);
            holder.tvUserPhone.setVisibility(View.VISIBLE);
        } else {
            holder.tvUserPhone.setVisibility(View.GONE);
        }

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(order.getTimestamp())));

        // Total
        holder.tvTotal.setText(String.format(Locale.getDefault(), "₹%.0f", order.getTotalAmount()));

        // Payment method
        String payment = order.getPaymentMethod();
        if (payment != null && !payment.isEmpty()) {
            String paymentDisplay;
            switch (payment.toLowerCase()) {
                case "upi": paymentDisplay = "UPI Payment"; break;
                case "card": paymentDisplay = "Credit / Debit Card"; break;
                default: paymentDisplay = "Cash on Delivery"; break;
            }
            holder.tvPaymentMethod.setText("💳 " + paymentDisplay);
            holder.tvPaymentMethod.setVisibility(View.VISIBLE);
        } else {
            holder.tvPaymentMethod.setVisibility(View.GONE);
        }

        // Status badge
        String status = order.getStatus() != null ? order.getStatus() : "Placed";
        holder.tvStatus.setText(status);

        switch (status.toLowerCase()) {
            case "accepted":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_accepted);
                holder.tvStatus.setTextColor(0xFF16A34A);
                break;
            case "completed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                holder.tvStatus.setTextColor(0xFF2563EB);
                break;
            case "rejected":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
                holder.tvStatus.setTextColor(0xFFEF4444);
                break;
            default: // "Placed"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_placed);
                holder.tvStatus.setTextColor(0xFFD97706);
                break;
        }

        // Build order items list
        holder.layoutItems.removeAllViews();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                View itemRow = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(android.R.layout.simple_list_item_1, holder.layoutItems, false);
                TextView tv = itemRow.findViewById(android.R.id.text1);
                tv.setTextSize(13);
                tv.setTextColor(0xFF6A7282);
                tv.setPadding(0, 4, 0, 4);
                tv.setText(String.format(Locale.getDefault(),
                        "%s × %d — ₹%.0f",
                        item.getProductName(),
                        item.getCount(),
                        item.getTotalPrice()));
                holder.layoutItems.addView(itemRow);
            }
        }

        // --- Action buttons logic ---
        boolean isPlaced = status.equalsIgnoreCase("Placed");
        boolean isAccepted = status.equalsIgnoreCase("Accepted");

        // Accept/Reject buttons: only for "Placed" orders
        holder.layoutActions.setVisibility(isPlaced ? View.VISIBLE : View.GONE);
        // Complete button: only for "Accepted" orders
        holder.btnComplete.setVisibility(isAccepted ? View.VISIBLE : View.GONE);

        if (isPlaced) {
            holder.btnAccept.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(order, holder.getAdapterPosition());
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onReject(order, holder.getAdapterPosition());
            });
        }

        if (isAccepted) {
            holder.btnComplete.setOnClickListener(v -> {
                if (listener != null) listener.onComplete(order, holder.getAdapterPosition());
            });
        }

        // Click whole card to open detail view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvUserName, tvUserAddress, tvUserPhone;
        TextView tvDate, tvTotal, tvPaymentMethod;
        LinearLayout layoutItems, layoutActions;
        TextView btnAccept, btnReject, btnComplete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_admin_order_id);
            tvStatus = itemView.findViewById(R.id.tv_admin_order_status);
            tvUserName = itemView.findViewById(R.id.tv_admin_user_name);
            tvUserAddress = itemView.findViewById(R.id.tv_admin_user_address);
            tvUserPhone = itemView.findViewById(R.id.tv_admin_user_phone);
            tvDate = itemView.findViewById(R.id.tv_admin_order_date);
            tvTotal = itemView.findViewById(R.id.tv_admin_order_total);
            tvPaymentMethod = itemView.findViewById(R.id.tv_admin_payment_method);
            layoutItems = itemView.findViewById(R.id.layout_admin_order_items);
            layoutActions = itemView.findViewById(R.id.layout_admin_actions);
            btnAccept = itemView.findViewById(R.id.btn_accept_order);
            btnReject = itemView.findViewById(R.id.btn_reject_order);
            btnComplete = itemView.findViewById(R.id.btn_complete_order);
        }
    }
}
