package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.famcart.R;
import com.example.testing.adapters.AdminOrderAdapter;
import com.example.testing.models.AdminOrder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Admin Orders panel — fetches ALL orders across ALL users from Firebase
 * and allows the admin to Accept, Reject, or Complete each order.
 *
 * Firebase structure:
 *   users/{userId}/orders/{orderId}/...
 *   users/{userId}/name, phone, address  (for display)
 *
 * Logout navigates back to LoginActivity (not exit).
 */
public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderActionListener {

    private static final String TAG = "AdminOrdersActivity";

    private RecyclerView rvOrders;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvOrderCount;
    private SwipeRefreshLayout swipeRefresh;
    private AdminOrderAdapter adapter;

    // All orders fetched from Firebase
    private List<AdminOrder> allOrders = new ArrayList<>();
    // Currently visible (filtered) orders
    private List<AdminOrder> filteredOrders = new ArrayList<>();
    private String currentFilter = "all";

    // Filter tab views
    private TextView tabAll, tabPlaced, tabAccepted, tabCompleted, tabRejected;

    // Firebase listener reference (so we can detach on destroy)
    private ValueEventListener ordersListener;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        initViews();
        setupTabs();

        adapter = new AdminOrderAdapter(this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        // Pull-to-refresh
        swipeRefresh.setColorSchemeColors(0xFF4F46E5);
        swipeRefresh.setOnRefreshListener(this::refreshOrders);

        loadAllOrders();
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rv_admin_orders);
        layoutEmpty = findViewById(R.id.layout_empty_admin);
        progressBar = findViewById(R.id.progress_bar_admin);
        tvOrderCount = findViewById(R.id.tv_order_count);
        swipeRefresh = findViewById(R.id.swipe_refresh_admin);

        tabAll = findViewById(R.id.tab_all);
        tabPlaced = findViewById(R.id.tab_placed);
        tabAccepted = findViewById(R.id.tab_accepted);
        tabCompleted = findViewById(R.id.tab_completed);
        tabRejected = findViewById(R.id.tab_rejected);

        // Back button — go back (doesn't exit app since LoginActivity is still in stack
        // unless we came from a cleared task; in that case, navigate explicitly)
        findViewById(R.id.btn_admin_back).setOnClickListener(v -> navigateToLogin());

        // Logout button — navigate to LoginActivity properly
        findViewById(R.id.btn_admin_logout).setOnClickListener(v -> {
            Toast.makeText(this, "Admin logged out", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        });
    }

    /**
     * Navigate back to LoginActivity instead of just finishing
     * (which would exit the app since the back stack was cleared).
     */
    private void navigateToLogin() {
        Intent intent = new Intent(AdminOrdersActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> applyFilter("all"));
        tabPlaced.setOnClickListener(v -> applyFilter("placed"));
        tabAccepted.setOnClickListener(v -> applyFilter("accepted"));
        tabCompleted.setOnClickListener(v -> applyFilter("completed"));
        tabRejected.setOnClickListener(v -> applyFilter("rejected"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;

        // Update tab visuals
        resetTabStyles();
        TextView activeTab;
        switch (filter) {
            case "placed": activeTab = tabPlaced; break;
            case "accepted": activeTab = tabAccepted; break;
            case "completed": activeTab = tabCompleted; break;
            case "rejected": activeTab = tabRejected; break;
            default: activeTab = tabAll; break;
        }
        activeTab.setBackgroundResource(R.drawable.bg_admin_button);
        activeTab.setTextColor(0xFFFFFFFF);

        // Filter orders
        filteredOrders.clear();
        for (AdminOrder order : allOrders) {
            if (filter.equals("all")) {
                filteredOrders.add(order);
            } else {
                String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "placed";
                if (status.equals(filter)) {
                    filteredOrders.add(order);
                }
            }
        }

        updateUI();
    }

    private void resetTabStyles() {
        TextView[] tabs = {tabAll, tabPlaced, tabAccepted, tabCompleted, tabRejected};
        for (TextView tab : tabs) {
            tab.setBackgroundResource(R.drawable.bg_input_field);
            tab.setTextColor(0xFF6A7282);
        }
    }

    private void updateUI() {
        if (filteredOrders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);

            // Dynamic empty state text
            TextView emptyTitle = findViewById(R.id.tv_empty_title);
            TextView emptySub = findViewById(R.id.tv_empty_subtitle);
            if (!currentFilter.equals("all")) {
                emptyTitle.setText("No " + currentFilter + " orders");
                emptySub.setText("There are no orders with \"" + currentFilter + "\" status.");
            } else {
                emptyTitle.setText("No orders found");
                emptySub.setText("Orders will appear here once customers start placing them.");
            }
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            swipeRefresh.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            adapter.updateOrders(new ArrayList<>(filteredOrders));
        }

        // Update count in header
        int totalCount = allOrders.size();
        int shownCount = filteredOrders.size();
        if (currentFilter.equals("all")) {
            tvOrderCount.setText(totalCount + " total order" + (totalCount != 1 ? "s" : ""));
        } else {
            tvOrderCount.setText(shownCount + " " + currentFilter + " order" + (shownCount != 1 ? "s" : "")
                    + " · " + totalCount + " total");
        }
    }

    /**
     * Pull-to-refresh handler — detaches old listener and re-fetches.
     */
    private void refreshOrders() {
        if (ordersListener != null && usersRef != null) {
            usersRef.removeEventListener(ordersListener);
        }
        loadAllOrders();
    }

    /**
     * Fetches all orders from ALL users in Firebase.
     * Walks: users/{userId}/orders/{orderId}
     * Also reads user profile fields for display.
     */
    private void loadAllOrders() {
        progressBar.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot usersSnapshot) {
                allOrders.clear();

                for (DataSnapshot userSnap : usersSnapshot.getChildren()) {
                    String userId = userSnap.getKey();

                    // Read user profile info
                    String userName = userSnap.child("name").getValue(String.class);
                    String userPhone = userSnap.child("phone").getValue(String.class);
                    String userAddress = userSnap.child("address").getValue(String.class);

                    // Iterate orders for this user
                    DataSnapshot ordersSnap = userSnap.child("orders");
                    for (DataSnapshot orderSnap : ordersSnap.getChildren()) {
                        try {
                            AdminOrder adminOrder = new AdminOrder();
                            adminOrder.setOrderId(orderSnap.getKey());
                            adminOrder.setUserId(userId);

                            // Prefer order-level data, fall back to profile-level
                            String orderUserName = orderSnap.child("userName").getValue(String.class);
                            adminOrder.setUserName(orderUserName != null && !orderUserName.isEmpty()
                                    ? orderUserName : userName);

                            String orderAddress = orderSnap.child("address").getValue(String.class);
                            adminOrder.setUserAddress(orderAddress != null && !orderAddress.isEmpty()
                                    ? orderAddress : userAddress);

                            String orderPhone = orderSnap.child("phone").getValue(String.class);
                            adminOrder.setUserPhone(orderPhone != null && !orderPhone.isEmpty()
                                    ? orderPhone : userPhone);

                            String paymentMethod = orderSnap.child("paymentMethod").getValue(String.class);
                            adminOrder.setPaymentMethod(paymentMethod);

                            // Read core order fields
                            Double total = orderSnap.child("totalAmount").getValue(Double.class);
                            adminOrder.setTotalAmount(total != null ? total : 0);

                            Long timestamp = orderSnap.child("timestamp").getValue(Long.class);
                            adminOrder.setTimestamp(timestamp != null ? timestamp : 0);

                            String status = orderSnap.child("status").getValue(String.class);
                            adminOrder.setStatus(status != null ? status : "Placed");

                            // Parse items list
                            DataSnapshot itemsSnap = orderSnap.child("items");
                            if (itemsSnap.exists()) {
                                List<com.example.testing.models.CartItem> items = new ArrayList<>();
                                for (DataSnapshot itemSnap : itemsSnap.getChildren()) {
                                    com.example.testing.models.CartItem item = itemSnap.getValue(
                                            com.example.testing.models.CartItem.class);
                                    if (item != null) {
                                        items.add(item);
                                    }
                                }
                                adminOrder.setItems(items);
                            }

                            allOrders.add(adminOrder);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order for user " + userId, e);
                        }
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(allOrders, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                applyFilter(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminOrdersActivity.this,
                        "Failed to load orders: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        };

        usersRef.addValueEventListener(ordersListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up Firebase listener
        if (ordersListener != null && usersRef != null) {
            usersRef.removeEventListener(ordersListener);
        }
    }

    // ─── Order Action Callbacks ─────────────────────────────────────────

    @Override
    public void onAccept(AdminOrder order, int position) {
        updateOrderStatus(order, "Accepted", position);
    }

    @Override
    public void onReject(AdminOrder order, int position) {
        updateOrderStatus(order, "Rejected", position);
    }

    @Override
    public void onComplete(AdminOrder order, int position) {
        updateOrderStatus(order, "Completed", position);
    }

    @Override
    public void onOrderClick(AdminOrder order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("admin_order", order);
        startActivity(intent);
    }

    /**
     * Updates the order status in Firebase under the user's orders path
     * and also mirrors it to a global /orders node.
     */
    private void updateOrderStatus(AdminOrder order, String newStatus, int position) {
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
                // Also mirror to global /orders node
                FirebaseDatabase.getInstance()
                        .getReference("orders")
                        .child(orderId)
                        .child("status")
                        .setValue(newStatus);

                // Update local data
                order.setStatus(newStatus);

                // Add notification for the user
                addNotificationForUser(userId, orderId, newStatus);

                Toast.makeText(this,
                        "Order " + newStatus.toLowerCase(),
                        Toast.LENGTH_SHORT).show();

                // Re-apply filter to refresh UI
                applyFilter(currentFilter);
            } else {
                Toast.makeText(this,
                        "Failed to update order",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Send a notification to the user whose order status was updated.
     */
    private void addNotificationForUser(String userId, String orderId, String status) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("notifications");

        String key = notifRef.push().getKey();
        if (key != null) {
            java.util.Map<String, Object> notification = new java.util.HashMap<>();

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
                    notification.put("message", "Your order #" + shortId + " has been completed. Thank you for shopping!");
                    break;
                case "Rejected":
                    notification.put("title", "Order Rejected");
                    notification.put("message", "Your order #" + shortId + " was rejected. Please contact support for details.");
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
