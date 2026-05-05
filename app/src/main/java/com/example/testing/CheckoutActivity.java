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

import com.bumptech.glide.Glide;
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

<<<<<<< HEAD
    private static final String TAG = "CheckoutActivity";
    private static final double DELIVERY_FEE = 25.0;
    private static final double FREE_DELIVERY_THRESHOLD = 199.0;
    private static final double TAX_RATE = 0.02;

    private LinearLayout layoutOrderItems, layoutRewards, layoutMembershipBenefit;
    private LinearLayout layoutPromoDiscount, layoutRewardsDiscount;
    private TextView tvSubtotal, tvDeliveryFee, tvTaxes, tvTotal, tvBottomTotal;
    private TextView tvPromoMessage, tvAvailableCoins, tvCoinsValue, tvMembershipText;
    private TextView tvPromoDiscount, tvRewardsDiscount;
    private EditText etAddress, etPromoCode;
    private TextView btnPlaceOrder, tvSelectedMethod, btnApplyPromo;
    private android.widget.CheckBox cbRedeemCoins;

    // Payment method radio views
    private View radioCod, radioUpi, radioCard;
    private String selectedPaymentMethod = "cod"; // default
=======
    private LinearLayout layoutOrderItems;
    private TextView tvSubtotal, tvTotal;
    private EditText etAddress;
    private TextView btnPlaceOrder;
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

    private List<CartItem> cartItems = new ArrayList<>();
    private double totalAmount = 0;
    
    // Logic state
    private double promoDiscountAmount = 0;
    private double rewardsDiscountAmount = 0;
    private double membershipDiscountAmount = 0;
    private int userCoins = 0;
    private boolean isGoldMember = false;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupClickListeners();
<<<<<<< HEAD
        setupPaymentMethodSelection();
        loadUserData();
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        loadCartForCheckout();
    }

    private void initViews() {
        layoutOrderItems = findViewById(R.id.layout_order_items);
        layoutRewards = findViewById(R.id.layout_rewards);
        layoutMembershipBenefit = findViewById(R.id.layout_membership_benefit);
        layoutPromoDiscount = findViewById(R.id.layout_promo_discount);
        layoutRewardsDiscount = findViewById(R.id.layout_rewards_discount);
        
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTotal = findViewById(R.id.tv_total);
<<<<<<< HEAD
        tvBottomTotal = findViewById(R.id.tv_bottom_total);
        tvPromoMessage = findViewById(R.id.tv_promo_message);
        tvAvailableCoins = findViewById(R.id.tv_available_coins);
        tvCoinsValue = findViewById(R.id.tv_coins_value);
        tvMembershipText = findViewById(R.id.tv_membership_text);
        tvPromoDiscount = findViewById(R.id.tv_promo_discount);
        tvRewardsDiscount = findViewById(R.id.tv_rewards_discount);
        
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        etAddress = findViewById(R.id.et_address);
        etPromoCode = findViewById(R.id.et_promo_code);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
<<<<<<< HEAD
        btnApplyPromo = findViewById(R.id.btn_apply_promo);
        tvSelectedMethod = findViewById(R.id.tv_selected_method);
        cbRedeemCoins = findViewById(R.id.cb_redeem_coins);

        radioCod = findViewById(R.id.radio_cod);
        radioUpi = findViewById(R.id.radio_upi);
        radioCard = findViewById(R.id.radio_card);

        // Select COD by default
        selectPaymentMethod("cod");
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        if (btnApplyPromo != null) {
            btnApplyPromo.setOnClickListener(v -> validatePromoCode());
        }
        if (cbRedeemCoins != null) {
            cbRedeemCoins.setOnCheckedChangeListener((buttonView, isChecked) -> populateOrderSummary());
        }
    }

    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Load Rewards
        userRef.child("rewards").child("coins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userCoins = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                if (tvAvailableCoins != null) tvAvailableCoins.setText("Redeem " + userCoins + " coins");
                if (tvCoinsValue != null) tvCoinsValue.setText("Save ₹" + (userCoins / 10.0) + " on this order");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Load Membership
        userRef.child("membership").child("isActive").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isGoldMember = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (layoutMembershipBenefit != null) {
                    layoutMembershipBenefit.setVisibility(isGoldMember ? View.VISIBLE : View.GONE);
                }
                populateOrderSummary();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void validatePromoCode() {
        if (etPromoCode == null) return;
        String code = etPromoCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            if (tvPromoMessage != null) {
                tvPromoMessage.setText("Enter a code");
                tvPromoMessage.setTextColor(0xFFE11D48);
                tvPromoMessage.setVisibility(View.VISIBLE);
            }
            return;
        }

        btnApplyPromo.setEnabled(false);
        FirebaseDatabase.getInstance().getReference("promoCodes").child(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        btnApplyPromo.setEnabled(true);
                        if (snapshot.exists()) {
                            Double discount = snapshot.child("discount").getValue(Double.class);
                            String type = snapshot.child("type").getValue(String.class);
                            
                            if (discount != null && type != null) {
                                if ("percentage".equals(type)) {
                                    promoDiscountAmount = (subtotalAmount * discount) / 100.0;
                                } else {
                                    promoDiscountAmount = discount;
                                }
                                if (tvPromoMessage != null) {
                                    tvPromoMessage.setText("Code " + code + " applied!");
                                    tvPromoMessage.setTextColor(0xFF16A34A);
                                    tvPromoMessage.setVisibility(View.VISIBLE);
                                }
                                populateOrderSummary();
                            }
                        } else {
                            promoDiscountAmount = 0;
                            if (tvPromoMessage != null) {
                                tvPromoMessage.setText("Invalid promo code");
                                tvPromoMessage.setTextColor(0xFFE11D48);
                                tvPromoMessage.setVisibility(View.VISIBLE);
                            }
                            populateOrderSummary();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        btnApplyPromo.setEnabled(true);
                    }
                });
    }

<<<<<<< HEAD
    private void setupPaymentMethodSelection() {
        View optCod = findViewById(R.id.option_cod);
        if (optCod != null) optCod.setOnClickListener(v -> selectPaymentMethod("cod"));

        View optUpi = findViewById(R.id.option_upi);
        if (optUpi != null) optUpi.setOnClickListener(v -> selectPaymentMethod("upi"));

        View optCard = findViewById(R.id.option_card);
        if (optCard != null) optCard.setOnClickListener(v -> selectPaymentMethod("card"));
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;

        // Reset all radio visuals
        if (radioCod != null) radioCod.setSelected(false);
        if (radioUpi != null) radioUpi.setSelected(false);
        if (radioCard != null) radioCard.setSelected(false);

        // Set selected
        switch (method) {
            case "cod":
                if (radioCod != null) radioCod.setSelected(true);
                if (tvSelectedMethod != null) tvSelectedMethod.setText("Selected: Cash on Delivery");
                break;
            case "upi":
                if (radioUpi != null) radioUpi.setSelected(true);
                if (tvSelectedMethod != null) tvSelectedMethod.setText("Selected: UPI Payment");
                break;
            case "card":
                if (radioCard != null) radioCard.setSelected(true);
                if (tvSelectedMethod != null) tvSelectedMethod.setText("Selected: Credit / Debit Card");
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

=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
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

<<<<<<< HEAD
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
                    if (etAddress != null) etAddress.setText(address);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        // Load cart items
        DatabaseReference cartRef = userRef.child("cart");
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
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
        if (layoutOrderItems == null) return;
        layoutOrderItems.removeAllViews();

        for (CartItem item : cartItems) {
<<<<<<< HEAD
            View row = LayoutInflater.from(this).inflate(R.layout.item_checkout_order, layoutOrderItems, false);

            TextView tvName = row.findViewById(R.id.tv_item_name);
            TextView tvQty = row.findViewById(R.id.tv_item_qty);
            TextView tvPrice = row.findViewById(R.id.tv_item_price);
            ImageView ivImage = row.findViewById(R.id.iv_item_image);

            if (tvName != null) tvName.setText(item.getProductName());
            if (tvQty != null) tvQty.setText(String.format(Locale.getDefault(), "%s × %d", item.getProductQuantity(), item.getCount()));
            if (tvPrice != null) tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", item.getTotalPrice()));

            if (ivImage != null) {
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    Glide.with(this)
                            .load(item.getImageUrl())
                            .placeholder(R.drawable.essentials)
                            .into(ivImage);
                } else if (item.getDrawableResId() != 0) {
                    ivImage.setImageResource(item.getDrawableResId());
                } else {
                    ivImage.setImageResource(R.drawable.essentials);
                }
            }

            layoutOrderItems.addView(row);
        }

        // --- CALCULATIONS ---
        
        // 1. Delivery Fee
        double delivery = subtotalAmount >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE;
        if (isGoldMember) {
            delivery = 0; // Free delivery for members
        }

        // 2. Membership Discount (5%)
        membershipDiscountAmount = isGoldMember ? (subtotalAmount * 0.05) : 0;

        // 3. Rewards Discount
        rewardsDiscountAmount = (cbRedeemCoins != null && cbRedeemCoins.isChecked()) ? (userCoins / 10.0) : 0;

        // 4. Taxes (on subtotal after membership discount)
        double taxes = (subtotalAmount - membershipDiscountAmount) * TAX_RATE;

        // 5. Total Calculation
        totalAmount = subtotalAmount + delivery + taxes - promoDiscountAmount - rewardsDiscountAmount - membershipDiscountAmount;
        if (totalAmount < 0) totalAmount = 0;

        // --- UI UPDATES ---
        
        if (tvSubtotal != null) tvSubtotal.setText(String.format(Locale.getDefault(), "₹%.0f", subtotalAmount));

        if (tvDeliveryFee != null) {
            if (delivery == 0) {
                tvDeliveryFee.setText("FREE");
                tvDeliveryFee.setTextColor(0xFF22C55E);
            } else {
                tvDeliveryFee.setText(String.format(Locale.getDefault(), "₹%.0f", delivery));
                tvDeliveryFee.setTextColor(0xFF101828);
            }
        }

        // Promo UI
        if (layoutPromoDiscount != null) {
            if (promoDiscountAmount > 0) {
                layoutPromoDiscount.setVisibility(View.VISIBLE);
                if (tvPromoDiscount != null) tvPromoDiscount.setText(String.format(Locale.getDefault(), "-₹%.2f", promoDiscountAmount));
            } else {
                layoutPromoDiscount.setVisibility(View.GONE);
            }
        }

        // Rewards UI
        if (layoutRewardsDiscount != null) {
            if (rewardsDiscountAmount > 0) {
                layoutRewardsDiscount.setVisibility(View.VISIBLE);
                if (tvRewardsDiscount != null) tvRewardsDiscount.setText(String.format(Locale.getDefault(), "-₹%.2f", rewardsDiscountAmount));
            } else {
                layoutRewardsDiscount.setVisibility(View.GONE);
            }
        }

        if (tvTaxes != null) tvTaxes.setText(String.format(Locale.getDefault(), "₹%.2f", taxes));
        if (tvTotal != null) tvTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
        if (tvBottomTotal != null) tvBottomTotal.setText(String.format(Locale.getDefault(), "₹%.2f", totalAmount));
        
        if (isGoldMember && tvMembershipText != null) {
            tvMembershipText.setText("Gold Member: Free delivery & 5% extra off applied! (Saved ₹" + 
                    String.format(Locale.getDefault(), "%.2f", membershipDiscountAmount + (subtotalAmount >= FREE_DELIVERY_THRESHOLD ? 0 : DELIVERY_FEE)) + ")");
        }
=======
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
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
    }

    private void placeOrder() {
        if (isProcessing) return;

        // Validate address
        if (etAddress == null) return;
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
        if (btnPlaceOrder != null) {
            btnPlaceOrder.setText("Placing Order...");
            btnPlaceOrder.setEnabled(false);
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            isProcessing = false;
<<<<<<< HEAD
            if (btnPlaceOrder != null) {
                btnPlaceOrder.setText("Place Order  →");
                btnPlaceOrder.setEnabled(true);
            }
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
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

<<<<<<< HEAD
                // Create order
                DatabaseReference ordersRef = userRef.child("orders");
                String orderId = ordersRef.push().getKey();

                if (orderId == null) {
                    isProcessing = false;
                    if (btnPlaceOrder != null) {
                        btnPlaceOrder.setText("Place Order  →");
                        btnPlaceOrder.setEnabled(true);
                    }
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
                orderData.put("subtotal", subtotalAmount);
                orderData.put("deliveryFee", subtotalAmount >= FREE_DELIVERY_THRESHOLD || isGoldMember ? 0 : DELIVERY_FEE);
                orderData.put("promoDiscount", promoDiscountAmount);
                orderData.put("rewardsDiscount", rewardsDiscountAmount);
                orderData.put("membershipDiscount", membershipDiscountAmount);
                orderData.put("totalAmount", totalAmount);
                orderData.put("timestamp", System.currentTimeMillis());
                orderData.put("status", "Placed");

                Log.d(TAG, "Placing order: " + orderId);

                // Update user rewards
                if (cbRedeemCoins != null && cbRedeemCoins.isChecked()) {
                    userRef.child("rewards").child("coins").setValue(0); // All used
                }
                // Reward for this order: totalAmount / 10
                final int earnedCoins = (int) (totalAmount / 10);
                userRef.child("rewards").child("coins").get().addOnCompleteListener(coinsTask -> {
                    int currentCoins = 0;
                    if (coinsTask.isSuccessful() && coinsTask.getResult().exists()) {
                        Integer val = coinsTask.getResult().getValue(Integer.class);
                        if (val != null) currentCoins = val;
                    }
                    userRef.child("rewards").child("coins").setValue(currentCoins + earnedCoins);
                });

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
                        if (btnPlaceOrder != null) {
                            btnPlaceOrder.setText("Place Order  →");
                            btnPlaceOrder.setEnabled(true);
                        }
                        Log.e(TAG, "Failed to place order", task.getException());
                        Toast.makeText(CheckoutActivity.this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isProcessing = false;
                if (btnPlaceOrder != null) {
                    btnPlaceOrder.setText("Place Order  →");
                    btnPlaceOrder.setEnabled(true);
                }
                Toast.makeText(CheckoutActivity.this, "Failed to read user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
=======
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
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        }
    }
}