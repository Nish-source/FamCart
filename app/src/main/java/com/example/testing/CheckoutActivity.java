package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";
    private static final double DELIVERY_FEE = 25.0;
    private static final double FREE_DELIVERY_THRESHOLD = 199.0;
    private static final double TAX_RATE = 0.02;

    private LinearLayout layoutOrderItems;
    private TextView tvSubtotal, tvDeliveryFee, tvTaxes, tvTotal, tvBottomTotal;
    private EditText etAddress;
    private TextView btnPlaceOrder, tvSelectedMethod;

    // Payment method radio views
    private View radioCod, radioUpi, radioCard;
    private String selectedPaymentMethod = "cod"; // default

    private List<CartItem> cartItems = new ArrayList<>();
    private double subtotalAmount = 0;
    private double totalAmount = 0;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Log.d(TAG, "CheckoutActivity created");

        initViews();
        setupClickListeners();
        setupPaymentMethodSelection();
        loadCartForCheckout();
    }

    private void initViews() {
        layoutOrderItems = findViewById(R.id.layout_order_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTaxes = findViewById(R.id.tv_taxes);
        tvTotal = findViewById(R.id.tv_total);
        tvBottomTotal = findViewById(R.id.tv_bottom_total);
        etAddress = findViewById(R.id.et_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        tvSelectedMethod = findViewById(R.id.tv_selected_method);

        radioCod = findViewById(R.id.radio_cod);
        radioUpi = findViewById(R.id.radio_upi);
        radioCard = findViewById(R.id.radio_card);

        // Select COD by default
        selectPaymentMethod("cod");
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void setupPaymentMethodSelection() {
        findViewById(R.id.option_cod).setOnClickListener(v -> selectPaymentMethod("cod"));
        findViewById(R.id.option_upi).setOnClickListener(v -> selectPaymentMethod("upi"));
        findViewById(R.id.option_card).setOnClickListener(v -> selectPaymentMethod("card"));
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;

        // Reset all radio visuals
        radioCod.setSelected(false);
        radioUpi.setSelected(false);
        radioCard.setSelected(false);

        // Set selected
        switch (method) {
            case "cod":
                radioCod.setSelected(true);
                tvSelectedMethod.setText("Selected: Cash on Delivery");
                break;
            case "upi":
                radioUpi.setSelected(true);
                tvSelectedMethod.setText("Selected: UPI Payment");
                break;
            case "card":
                radioCard.setSelected(true);
                tvSelectedMethod.setText("Selected: Credit / Debit Card");
                break;
        }

        // Save preference to Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("preferredPayment")
                    .setValue(method);
        }
    }

    private void loadCartForCheckout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // Load preferred payment method
        userRef.child("preferredPayment").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String method = snapshot.getValue(String.class);
                if (method != null) {
                    selectPaymentMethod(method);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Load saved address
        userRef.child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.getValue(String.class);
                if (address != null && !address.isEmpty()) {
                    etAddress.setText(address);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Load cart items
        DatabaseReference cartRef = userRef.child("cart");
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                subtotalAmount = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    CartItem item = child.getValue(CartItem.class);
                    if (item != null) {
                        item.setCartItemId(child.getKey());
                        cartItems.add(item);
                        subtotalAmount += item.getTotalPrice();
                    }
                }

                if (cartItems.isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Log.d(TAG, "Loaded " + cartItems.size() + " items, subtotal: " + subtotalAmount);
                populateOrderSummary();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load cart: " + error.getMessage());
                Toast.makeText(CheckoutActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateOrderSummary() {
        layoutOrderItems.removeAllViews();

        for (CartItem item : cartItems) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_checkout_order, layoutOrderItems, false);

            TextView tvName = row.findViewById(R.id.tv_item_name);
            TextView tvQty = row.findViewById(R.id.tv_item_qty);
            TextView tvPrice = row.findViewById(R.id.tv_item_price);
            ImageView ivImage = row.findViewById(R.id.iv_item_image);

            tvName.setText(item.getProductName());
            tvQty.setText(String.format(Locale.getDefault(), "%s × %d", item.getProductQuantity(), item.getCount()));
            tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", item.getTotalPrice()));

            if (item.getDrawableResId() != 0) {
                ivImage.setImageResource(item.getDrawableResId());
            }

            layoutOrderItems.addView(row);
        }

        // Calculate delivery and taxes
        double delivery = subtotalAmount >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
        double taxes = subtotalAmount * TAX_RATE;
        totalAmount = subtotalAmount + delivery + taxes;

        tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.0f", subtotalAmount));

        if (delivery == 0) {
            tvDeliveryFee.setText("FREE");
            tvDeliveryFee.setTextColor(0xFF22C55E);
        } else {
            tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.0f", delivery));
            tvDeliveryFee.setTextColor(0xFF101828);
        }

        tvTaxes.setText(String.format(Locale.getDefault(), "₹%.2f", taxes));
        tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
        tvBottomTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
    }

    private void placeOrder() {
        if (isProcessing) return;

        // Validate address
        String address = etAddress.getText().toString().trim();
        if (address.isEmpty()) {
            etAddress.setError("Please enter a delivery address");
            etAddress.requestFocus();
            return;
        }

        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true;
        btnPlaceOrder.setText("Placing Order...");
        btnPlaceOrder.setEnabled(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            isProcessing = false;
            btnPlaceOrder.setText("Place Order  →");
            btnPlaceOrder.setEnabled(true);
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // Save the address for future use
        userRef.child("address").setValue(address);

        // Read user profile data (name, phone) then create the order
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String userPhone = snapshot.child("phone").getValue(String.class);

                // Create order
                DatabaseReference ordersRef = userRef.child("orders");
                String orderId = ordersRef.push().getKey();

                if (orderId == null) {
                    isProcessing = false;
                    btnPlaceOrder.setText("Place Order  →");
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, "Failed to create order. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Build enriched order map with all details the admin needs
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("orderId", orderId);
                orderData.put("userId", userId);
                orderData.put("userName", userName != null ? userName : "");
                orderData.put("address", address);
                orderData.put("phone", userPhone != null ? userPhone : "");
                orderData.put("paymentMethod", selectedPaymentMethod);
                orderData.put("items", new ArrayList<>(cartItems));
                orderData.put("totalAmount", totalAmount);
                orderData.put("timestamp", System.currentTimeMillis());
                orderData.put("status", "Placed");

                Log.d(TAG, "Placing order: " + orderId);

                ordersRef.child(orderId).setValue(orderData).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Order saved successfully, clearing cart");

                        // Also mirror to global /orders node for admin access
                        FirebaseDatabase.getInstance()
                                .getReference("orders")
                                .child(orderId)
                                .setValue(orderData);

                        // Add notification for this order
                        addNotification(userId, "Order Placed",
                                "Your order #" + orderId.substring(0, Math.min(8, orderId.length())).toUpperCase()
                                        + " has been placed. Total: ₹" + String.format(Locale.getDefault(), "%.2f", totalAmount));

                        // Clear cart after successful order
                        userRef.child("cart").removeValue().addOnCompleteListener(clearTask -> {
                            isProcessing = false;

                            // Navigate to Order Placed screen
                            Intent intent = new Intent(CheckoutActivity.this, OrderPlacedActivity.class);
                            intent.putExtra("order_id", orderId);
                            intent.putExtra("total_amount", totalAmount);
                            intent.putExtra("address", address);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        isProcessing = false;
                        btnPlaceOrder.setText("Place Order  →");
                        btnPlaceOrder.setEnabled(true);
                        Log.e(TAG, "Failed to place order", task.getException());
                        Toast.makeText(CheckoutActivity.this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isProcessing = false;
                btnPlaceOrder.setText("Place Order  →");
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, "Failed to read user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add an in-app notification entry to Firebase for this user.
     */
    private void addNotification(String userId, String title, String message) {
        DatabaseReference notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("notifications");

        String key = notifRef.push().getKey();
        if (key != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());
            notification.put("read", false);
            notifRef.child(key).setValue(notification);
        }
    }
}
