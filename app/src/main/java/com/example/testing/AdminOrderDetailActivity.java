package com.example.testing;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.AdminOrder;
import com.example.testing.models.CartItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Detailed view of a single admin order.
 * Shows full customer info, address, itemized list, payment, and action buttons.
 */
public class AdminOrderDetailActivity extends AppCompatActivity {

    private AdminOrder order;

    // Views
    private TextView tvOrderId, tvDate, tvStatus;
    private TextView tvCustomerName, tvCustomerPhone, tvUserId;
    private TextView tvAddress, tvTotal, tvPayment;
    private LinearLayout layoutItems, layoutActions;
    private TextView btnAccept, btnReject, btnComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        // Get order from intent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            order = getIntent().getSerializableExtra("admin_order", AdminOrder.class);
        } else {
            order = (AdminOrder) getIntent().getSerializableExtra("admin_order");
        }
        if (order == null) {
            Toast.makeText(this, "Order data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        populateData();
        setupActions();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tv_detail_order_id);
        tvDate = findViewById(R.id.tv_detail_date);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvCustomerName = findViewById(R.id.tv_detail_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_detail_customer_phone);
        tvUserId = findViewById(R.id.tv_detail_user_id);
        tvAddress = findViewById(R.id.tv_detail_address);
        tvTotal = findViewById(R.id.tv_detail_total);
        tvPayment = findViewById(R.id.tv_detail_payment);
        layoutItems = findViewById(R.id.layout_detail_items);
        layoutActions = findViewById(R.id.layout_detail_actions);
        btnAccept = findViewById(R.id.btn_detail_accept);
        btnReject = findViewById(R.id.btn_detail_reject);
        btnComplete = findViewById(R.id.btn_detail_complete);

        findViewById(R.id.btn_detail_back).setOnClickListener(v -> finish());
    }

    private void populateData() {
        // Order ID
        String shortId = order.getOrderId();
        if (shortId != null && shortId.length() > 8) {
            shortId = shortId.substring(0, 8).toUpperCase();
        }
        tvOrderId.setText("Order #" + shortId);

        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy 'at' hh:mm a", Locale.getDefault());
        tvDate.setText(sdf.format(new Date(order.getTimestamp())));

        // Status badge
        updateStatusBadge(order.getStatus());

        // Customer info
        tvCustomerName.setText(order.getUserName() != null && !order.getUserName().isEmpty()
                ? order.getUserName() : "Not available");
        tvCustomerPhone.setText(order.getUserPhone() != null && !order.getUserPhone().isEmpty()
                ? order.getUserPhone() : "Not available");
        tvUserId.setText(order.getUserId() != null ? order.getUserId() : "Unknown");

        // Address
        tvAddress.setText(order.getUserAddress() != null && !order.getUserAddress().isEmpty()
                ? order.getUserAddress() : "No address provided");

        // Total
        tvTotal.setText(String.format(Locale.getDefault(), "₹%.0f", order.getTotalAmount()));

        // Payment method
        String payment = order.getPaymentMethod();
        if (payment != null && !payment.isEmpty()) {
            switch (payment.toLowerCase()) {
                case "upi": tvPayment.setText("UPI Payment"); break;
                case "card": tvPayment.setText("Credit / Debit Card"); break;
                default: tvPayment.setText("Cash on Delivery"); break;
            }
        } else {
            tvPayment.setText("Cash on Delivery");
        }

        // Order items
        layoutItems.removeAllViews();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_2, layoutItems, false);

                TextView line1 = row.findViewById(android.R.id.text1);
                TextView line2 = row.findViewById(android.R.id.text2);

                line1.setText(item.getProductName());
                line1.setTextSize(14);
                line1.setTextColor(0xFF101828);

                String qty = item.getProductQuantity() != null ? item.getProductQuantity() : "";
                line2.setText(String.format(Locale.getDefault(),
                        "%s × %d  ·  ₹%.0f",
                        qty, item.getCount(), item.getTotalPrice()));
                line2.setTextSize(12);
                line2.setTextColor(0xFF6A7282);

                layoutItems.addView(row);

                // Add divider between items (not after last)
                if (i < order.getItems().size() - 1) {
                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(0xFFF3F4F6);
                    layoutItems.addView(divider);
                }
            }
        }
    }

    private void setupActions() {
        String status = order.getStatus() != null ? order.getStatus() : "Placed";

        if (status.equalsIgnoreCase("Placed")) {
            layoutActions.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.GONE);

            btnAccept.setOnClickListener(v -> updateStatus("Accepted"));
            btnReject.setOnClickListener(v -> updateStatus("Rejected"));
        } else if (status.equalsIgnoreCase("Accepted")) {
            layoutActions.setVisibility(View.GONE);
            btnComplete.setVisibility(View.VISIBLE);

            btnComplete.setOnClickListener(v -> updateStatus("Completed"));
        } else {
            layoutActions.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
        }
    }

    private void updateStatus(String newStatus) {
        String userId = order.getUserId();
        String orderId = order.getOrderId();

        if (userId == null || orderId == null) {
            Toast.makeText(this, "Invalid order data", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userOrderRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("orders")
                .child(orderId)
                .child("status");

        userOrderRef.setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Mirror to global /orders
                FirebaseDatabase.getInstance()
                        .getReference("orders")
                        .child(orderId)
                        .child("status")
                        .setValue(newStatus);

                // Send notification to user
                addNotificationForUser(userId, orderId, newStatus);

                // Update local state and UI
                order.setStatus(newStatus);
                updateStatusBadge(newStatus);
                setupActions();

                Toast.makeText(this, "Order " + newStatus.toLowerCase(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusBadge(String status) {
        if (status == null) status = "Placed";
        tvStatus.setText(status);

        switch (status.toLowerCase()) {
            case "accepted":
                tvStatus.setBackgroundResource(R.drawable.bg_status_accepted);
                tvStatus.setTextColor(0xFF16A34A);
                break;
            case "completed":
                tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                tvStatus.setTextColor(0xFF2563EB);
                break;
            case "rejected":
                tvStatus.setBackgroundResource(R.drawable.bg_status_rejected);
                tvStatus.setTextColor(0xFFEF4444);
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.bg_status_placed);
                tvStatus.setTextColor(0xFFD97706);
                break;
        }
    }

    /**
     * Send notification to the customer about their order status change.
     */
    private void addNotificationForUser(String userId, String orderId, String status) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("notifications");

        String key = notifRef.push().getKey();
        if (key != null) {
            Map<String, Object> notification = new HashMap<>();
            String shortId = orderId.length() > 8
                    ? orderId.substring(0, 8).toUpperCase()
                    : orderId.toUpperCase();

            switch (status) {
                case "Accepted":
                    notification.put("title", "Order Accepted ✅");
                    notification.put("message", "Your order #" + shortId + " has been accepted and is being prepared!");
                    break;
                case "Completed":
                    notification.put("title", "Order Completed 🎉");
                    notification.put("message", "Your order #" + shortId + " has been completed. Thank you!");
                    break;
                case "Rejected":
                    notification.put("title", "Order Rejected");
                    notification.put("message", "Your order #" + shortId + " was rejected. Please contact support.");
                    break;
                default:
                    notification.put("title", "Order Updated");
                    notification.put("message", "Your order #" + shortId + " status: " + status);
                    break;
            }
            notification.put("timestamp", System.currentTimeMillis());
            notification.put("read", false);

            notifRef.child(key).setValue(notification);
        }
    }
}
