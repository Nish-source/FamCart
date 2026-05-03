package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.CartAdapter;
import com.example.testing.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private static final String TAG = "CartActivity";
    private static final double DELIVERY_FEE = 25.0;
    private static final double FREE_DELIVERY_THRESHOLD = 199.0;
    private static final double TAX_RATE = 0.02; // 2% tax

    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private LinearLayout layoutBottomBar;
    private LinearLayout layoutOrderSummary;
    private LinearLayout layoutPromo;
    private LinearLayout layoutDeliveryBanner;
    private TextView tvTotalPrice, tvItemCount, btnCheckout;
    private TextView tvSubtotal, tvDeliveryFee, tvTaxes, tvDeliveryMessage;

    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private DatabaseReference cartRef;
    private ValueEventListener cartListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rv_cart_items);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        layoutBottomBar = findViewById(R.id.layout_bottom_bar);
        layoutOrderSummary = findViewById(R.id.layout_order_summary);
        layoutPromo = findViewById(R.id.layout_promo);
        layoutDeliveryBanner = findViewById(R.id.layout_delivery_banner);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvItemCount = findViewById(R.id.tv_item_count);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTaxes = findViewById(R.id.tv_taxes);
        tvDeliveryMessage = findViewById(R.id.tv_delivery_message);
        btnCheckout = findViewById(R.id.btn_checkout);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_browse_products).setOnClickListener(v -> {
            finish(); // Go back to home
        });

        // Promo code apply button
        findViewById(R.id.btn_apply_promo).setOnClickListener(v -> {
            EditText etPromo = findViewById(R.id.et_promo_code);
            String code = etPromo.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter a promo code", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Promo code applied!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCheckout.setOnClickListener(v -> {
            Log.d(TAG, "Checkout clicked, cart size: " + cartItems.size());
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                startActivity(intent);
                Log.d(TAG, "Checkout intent started successfully");
            } catch (Exception e) {
                Log.e(TAG, "Failed to start CheckoutActivity", e);
                Toast.makeText(this, "Unable to open checkout", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCart() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");

        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CartItem item = child.getValue(CartItem.class);
                    if (item != null) {
                        item.setCartItemId(child.getKey());
                        cartItems.add(item);
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        };
        cartRef.addValueEventListener(cartListener);
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            showEmptyState();
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            layoutBottomBar.setVisibility(View.VISIBLE);
            layoutOrderSummary.setVisibility(View.VISIBLE);
            layoutPromo.setVisibility(View.VISIBLE);

            // Also show the scroll content
            View scrollContent = findViewById(R.id.scroll_content);
            if (scrollContent != null) scrollContent.setVisibility(View.VISIBLE);

            adapter.updateItems(cartItems);

            // Calculate totals
            double subtotal = 0;
            int totalQty = 0;
            for (CartItem item : cartItems) {
                subtotal += item.getTotalPrice();
                totalQty += item.getCount();
            }

            double delivery = subtotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
            double taxes = subtotal * TAX_RATE;
            double total = subtotal + delivery + taxes;

            // Update labels
            tvItemCount.setText(totalQty + " items");
            tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.0f", subtotal));

            if (delivery == 0) {
                tvDeliveryFee.setText("FREE");
                tvDeliveryFee.setTextColor(0xFF22C55E);
                layoutDeliveryBanner.setVisibility(View.GONE);
            } else {
                tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.0f", delivery));
                tvDeliveryFee.setTextColor(0xFF101828);
                layoutDeliveryBanner.setVisibility(View.VISIBLE);
                double remaining = FREE_DELIVERY_THRESHOLD - subtotal;
                tvDeliveryMessage.setText(String.format(Locale.getDefault(),
                        "Add ₹%.0f more for free delivery", remaining));
            }

            tvTaxes.setText(String.format(Locale.getDefault(), "₹%.2f", taxes));
            tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        }
    }

    private void showEmptyState() {
        rvCartItems.setVisibility(View.GONE);
        layoutEmptyCart.setVisibility(View.VISIBLE);
        layoutBottomBar.setVisibility(View.GONE);
        layoutOrderSummary.setVisibility(View.GONE);
        layoutPromo.setVisibility(View.GONE);

        // Hide scroll content sections
        View scrollContent = findViewById(R.id.scroll_content);
        if (scrollContent != null) scrollContent.setVisibility(View.GONE);
    }

    @Override
    public void onQuantityChanged(CartItem item, int newCount) {
        if (cartRef != null && item.getCartItemId() != null) {
            if (newCount > 10) {
                Toast.makeText(this, "Maximum 10 items allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            // Update count in Firebase — the ValueEventListener will auto-refresh UI
            cartRef.child(item.getCartItemId()).child("count").setValue(newCount);
        }
    }

    @Override
    public void onItemRemoved(CartItem item) {
        if (cartRef != null && item.getCartItemId() != null) {
            cartRef.child(item.getCartItemId()).removeValue();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up Firebase listener
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
    }
}