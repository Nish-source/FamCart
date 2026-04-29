package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.CartItem;
import com.example.testing.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private LinearLayout layoutOrderItems;
    private TextView tvSubtotal, tvTotal;
    private EditText etAddress;
    private TextView btnPlaceOrder;

    private List<CartItem> cartItems = new ArrayList<>();
    private double totalAmount = 0;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupClickListeners();
        loadCartForCheckout();
    }

    private void initViews() {
        layoutOrderItems = findViewById(R.id.layout_order_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
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
                totalAmount = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    CartItem item = child.getValue(CartItem.class);
                    if (item != null) {
                        item.setCartItemId(child.getKey());
                        cartItems.add(item);
                        totalAmount += item.getTotalPrice();
                    }
                }

                if (cartItems.isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                populateOrderSummary();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CheckoutActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateOrderSummary() {
        layoutOrderItems.removeAllViews();

        for (CartItem item : cartItems) {
            View row = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, layoutOrderItems, false);
            TextView tv = row.findViewById(android.R.id.text1);
            tv.setTextSize(13);
            tv.setTextColor(0xFF6A7282);
            tv.setPadding(0, 8, 0, 8);
            tv.setText(String.format(Locale.getDefault(),
                    "%s × %d — ₹%.0f",
                    item.getProductName(),
                    item.getCount(),
                    item.getTotalPrice()));
            layoutOrderItems.addView(row);
        }

        tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.0f", totalAmount));
        tvTotal.setText(String.format(Locale.getDefault(), "₹%.0f", totalAmount));
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
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        // Create order
        DatabaseReference ordersRef = userRef.child("orders");
        String orderId = ordersRef.push().getKey();

        Order order = new Order(
                orderId,
                new ArrayList<>(cartItems),
                totalAmount,
                System.currentTimeMillis(),
                "Placed"
        );

        if (orderId != null) {
            ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Clear cart after successful order
                    userRef.child("cart").removeValue().addOnCompleteListener(clearTask -> {
                        isProcessing = false;
                        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();

                        // Navigate to orders screen
                        Intent intent = new Intent(this, OrdersActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    isProcessing = false;
                    btnPlaceOrder.setText("Place Order");
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
