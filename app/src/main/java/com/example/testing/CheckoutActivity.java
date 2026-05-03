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
import com.example.testing.models.Order;
import com.example.testing.utils.ImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckoutActivity extends AppCompatActivity {

    private static final String TAG = "CheckoutActivity";
    private static final double DELIVERY_FEE = 25.0;
    private static final double FREE_DELIVERY_THRESHOLD = 199.0;
    private static final double TAX_RATE = 0.02;

    private LinearLayout layoutOrderItems;
    private TextView tvSubtotal, tvDeliveryFee, tvTotal;
    private EditText etAddress;
    private TextView btnPlaceOrder;

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
        loadCartForCheckout();
    }

    private void initViews() {
        layoutOrderItems = findViewById(R.id.layout_order_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTotal = findViewById(R.id.tv_total);
        etAddress = findViewById(R.id.et_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadCartForCheckout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");

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

            ImageLoader.loadCartItem(this, item, ivImage);

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

        tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
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
            resetButton();
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }


        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // Create order
        DatabaseReference ordersRef = userRef.child("orders");
        String orderId = ordersRef.push().getKey();

        if (orderId == null) {
            resetButton();
            Toast.makeText(this, "Failed to create order. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Order order = new Order(
                orderId,
                new ArrayList<>(cartItems),
                totalAmount,
                System.currentTimeMillis(),
                "Placed"
        );

        Log.d(TAG, "Placing order: " + orderId);

        ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                resetButton();
                Log.e(TAG, "Failed to place order", task.getException());
                Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Order saved: " + orderId);

            // ── Step 2: Reduce product quantities in Firebase ──
            reduceProductQuantities(cartItems, () -> {

                // ── Step 3: Clear the cart ──
                userRef.child("cart").removeValue().addOnCompleteListener(clearTask -> {
                    isProcessing = false;

                    Intent intent = new Intent(CheckoutActivity.this, OrderPlacedActivity.class);
                    intent.putExtra("order_id", orderId);
                    intent.putExtra("total_amount", totalAmount);
                    intent.putExtra("address", address);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            });
        });
    }


    private void reduceProductQuantities(List<CartItem> items, Runnable onComplete) {
        if (items.isEmpty()) {
            onComplete.run();
            return;
        }

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Products");

        // AtomicInteger lets us count async callbacks safely across multiple Firebase calls
        AtomicInteger pendingCount = new AtomicInteger(items.size());

        for (CartItem item : items) {
            String productId = item.getProductId();
            if (productId == null || productId.isEmpty()) {
                // No product ID — skip and check if all done
                if (pendingCount.decrementAndGet() == 0) onComplete.run();
                continue;
            }

            productsRef.child(productId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Try to read quantity as a number
                                Object rawQty = snapshot.child("stockQuantity").getValue();
                                if (rawQty instanceof Long) {
                                    long currentQty = (Long) rawQty;
                                    long newQty = Math.max(0, currentQty - item.getCount());
                                    productsRef.child(productId).child("stockQuantity").setValue(newQty)
                                            .addOnCompleteListener(t -> {
                                                Log.d(TAG, "Updated qty for " + productId
                                                        + ": " + currentQty + " → " + newQty);
                                                if (pendingCount.decrementAndGet() == 0) onComplete.run();
                                            });
                                } else if (rawQty instanceof Double) {
                                    // Firebase sometimes reads integers as Double
                                    double currentQty = (Double) rawQty;
                                    double newQty = Math.max(0, currentQty - item.getCount());
                                    productsRef.child(productId).child("quantity").setValue((long) newQty)
                                            .addOnCompleteListener(t -> {
                                                Log.d(TAG, "Updated qty (double) for " + productId);
                                                if (pendingCount.decrementAndGet() == 0) onComplete.run();
                                            });
                                } else {
                                    // Quantity is a String like "500ml" — do not modify it
                                    Log.d(TAG, "Skipping quantity reduction for " + productId
                                            + " (non-numeric: " + rawQty + ")");
                                    if (pendingCount.decrementAndGet() == 0) onComplete.run();
                                }
                            } else {
                                // Product not found in Firebase — skip
                                Log.w(TAG, "Product not found in Firebase: " + productId);
                                if (pendingCount.decrementAndGet() == 0) onComplete.run();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error reading product " + productId + ": " + error.getMessage());
                            // Don't block checkout on a single failure
                            if (pendingCount.decrementAndGet() == 0) onComplete.run();
                        }
                    });
        }
    }

    private void resetButton() {
        isProcessing = false;
        btnPlaceOrder.setText("Place Order");
        btnPlaceOrder.setEnabled(true);
    }
}
