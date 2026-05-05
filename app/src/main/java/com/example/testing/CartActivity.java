package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private LinearLayout layoutBottomBar;
<<<<<<< HEAD
    private LinearLayout layoutOrderSummary;
    private LinearLayout layoutPromo, layoutPromoDiscountRow;
    private LinearLayout layoutDeliveryBanner;
    private TextView tvTotalPrice, tvTotalPriceBtn, tvItemCount;
    private View btnCheckout;
    private TextView tvSubtotal, tvDeliveryFee, tvTaxes, tvDeliveryMessage, tvPromoDiscount;
=======
    private TextView tvTotalPrice, tvItemCount, btnCheckout;
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private double promoDiscount = 0;
    private String appliedPromo = "";
    private DatabaseReference cartRef;

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
<<<<<<< HEAD
        layoutOrderSummary = findViewById(R.id.layout_order_summary);
        layoutPromo = findViewById(R.id.layout_promo);
        layoutPromoDiscountRow = findViewById(R.id.layout_promo_discount_row);
        layoutDeliveryBanner = findViewById(R.id.layout_delivery_banner);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvItemCount = findViewById(R.id.tv_item_count);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTaxes = findViewById(R.id.tv_taxes);
        tvPromoDiscount = findViewById(R.id.tv_promo_discount);
        tvDeliveryMessage = findViewById(R.id.tv_delivery_message);
=======
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvItemCount = findViewById(R.id.tv_item_count);
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        btnCheckout = findViewById(R.id.btn_checkout);
        tvTotalPriceBtn = findViewById(R.id.tv_total_price_btn);
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

<<<<<<< HEAD
        // Promo code apply button
        findViewById(R.id.btn_apply_promo).setOnClickListener(v -> {
            EditText etPromo = findViewById(R.id.et_promo_code);
            String code = etPromo.getText().toString().trim().toUpperCase();
            if (code.isEmpty()) {
                Toast.makeText(this, "Please enter a promo code", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseDatabase.getInstance().getReference("promoCodes").child(code)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Double discount = snapshot.child("discount").getValue(Double.class);
                                String type = snapshot.child("type").getValue(String.class);
                                if (discount != null && type != null) {
                                    appliedPromo = code;
                                    double subtotal = 0;
                                    for (CartItem item : cartItems) subtotal += item.getTotalPrice();
                                    
                                    if ("percentage".equals(type)) {
                                        promoDiscount = (subtotal * discount) / 100.0;
                                    } else {
                                        promoDiscount = discount;
                                    }
                                    Toast.makeText(CartActivity.this, "Code " + code + " applied!", Toast.LENGTH_SHORT).show();
                                    updateUI();
                                }
                            } else {
                                promoDiscount = 0;
                                appliedPromo = "";
                                Toast.makeText(CartActivity.this, "Invalid promo code", Toast.LENGTH_SHORT).show();
                                updateUI();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        });

=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
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

        cartRef.addValueEventListener(new ValueEventListener() {
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
        });
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            showEmptyState();
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            layoutBottomBar.setVisibility(View.VISIBLE);
            adapter.updateItems(cartItems);

            // Calculate total
            double total = 0;
            for (CartItem item : cartItems) {
                total += item.getTotalPrice();
            }
<<<<<<< HEAD

            double delivery = subtotal >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
            double taxes = subtotal * TAX_RATE;
            double total = subtotal + delivery + taxes - promoDiscount;
            if (total < 0) total = 0;

            // Update labels
            tvItemCount.setText(totalQty + " items");
            tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.0f", subtotal));

            if (promoDiscount > 0) {
                if (layoutPromoDiscountRow != null) layoutPromoDiscountRow.setVisibility(View.VISIBLE);
                if (tvPromoDiscount != null) tvPromoDiscount.setText(String.format(Locale.getDefault(), "-₹%.2f", promoDiscount));
            } else {
                if (layoutPromoDiscountRow != null) layoutPromoDiscountRow.setVisibility(View.GONE);
            }

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
            String totalStr = String.format(Locale.getDefault(), "₹%.2f", total);
            tvTotalPrice.setText(totalStr);
            if (tvTotalPriceBtn != null) tvTotalPriceBtn.setText(totalStr);
=======
            tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.0f", total));
            tvItemCount.setText(cartItems.size() + " items");
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        }
    }

    private void showEmptyState() {
        rvCartItems.setVisibility(View.GONE);
        layoutEmptyCart.setVisibility(View.VISIBLE);
        layoutBottomBar.setVisibility(View.GONE);
    }

    @Override
    public void onQuantityChanged(CartItem item, int newCount) {
        if (cartRef != null && item.getCartItemId() != null) {
            cartRef.child(item.getCartItemId()).child("count").setValue(newCount);
        }
    }

    @Override
    public void onItemRemoved(CartItem item) {
        if (cartRef != null && item.getCartItemId() != null) {
            cartRef.child(item.getCartItemId()).removeValue();
        }
    }
<<<<<<< HEAD

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up Firebase listener
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
    }
}
=======
}
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
