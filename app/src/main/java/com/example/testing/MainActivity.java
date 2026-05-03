package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvDeliverToLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvDeliverToLabel = findViewById(R.id.tv_deliver_to_label);

        setupBottomNav();
        setupSearchBar();
        setupProductCardClicks();
        setupHeaderClicks();
        updateCartBadge();
        loadSavedAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        loadSavedAddress();
    }

    private void setupBottomNav() {
        // Home tab - already on home
        findViewById(R.id.btn_tab_home).setOnClickListener(v -> {
            // Already on home, scroll to top
            findViewById(R.id.scroll_view_content).scrollTo(0, 0);
        });

        // Search tab
        findViewById(R.id.btn_tab_search).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });

        // Cart tab
        findViewById(R.id.btn_tab_cart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });

        // Orders tab
        findViewById(R.id.btn_tab_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
        });

        // Profile tab
        findViewById(R.id.btn_tab_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    private void setupSearchBar() {
        // Search bar click opens search activity
        findViewById(R.id.btn_search_bar).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
    }

    private void setupHeaderClicks() {
        // Profile avatar in header
        try {
            findViewById(R.id.btn_profile_avatar).setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        } catch (Exception ignored) {}

        // Notification bell button → open NotificationsActivity
        try {
            findViewById(R.id.btn_notifications).setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationsActivity.class));
            });
        } catch (Exception ignored) {}

        // Address selector → open address bottom sheet
        try {
            findViewById(R.id.btn_address_selector).setOnClickListener(v -> {
                showAddressBottomSheet();
            });
        } catch (Exception ignored) {}
    }

    /**
     * Shows a bottom sheet dialog for address selection/entry.
     * User can view their saved address or enter a new one.
     */
    private void showAddressBottomSheet() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            android.widget.Toast.makeText(this, "Please login first", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);

        // Build a BottomSheetDialog with an EditText for address
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        // Create the content view programmatically
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(32));
        layout.setBackgroundColor(0xFFFFFFFF);

        // Title
        TextView title = new TextView(this);
        title.setText("Delivery Address");
        title.setTextSize(18);
        title.setTextColor(0xFF101828);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Enter or update your delivery address");
        subtitle.setTextSize(13);
        subtitle.setTextColor(0xFF99A1AF);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        layout.addView(subtitle);

        // Address input
        android.widget.EditText etAddress = new android.widget.EditText(this);
        etAddress.setHint("Enter your full delivery address");
        etAddress.setTextSize(14);
        etAddress.setTextColor(0xFF101828);
        etAddress.setHintTextColor(0xFF99A1AF);
        etAddress.setMinLines(2);
        etAddress.setMaxLines(4);
        etAddress.setPadding(dp(16), dp(14), dp(16), dp(14));
        etAddress.setBackground(getDrawable(R.drawable.bg_input_field));
        layout.addView(etAddress);

        // Spacer
        View spacer = new View(this);
        spacer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(16)));
        layout.addView(spacer);

        // Save button
        TextView btnSave = new TextView(this);
        btnSave.setText("Save Address");
        btnSave.setTextSize(15);
        btnSave.setTextColor(0xFFFFFFFF);
        btnSave.setGravity(android.view.Gravity.CENTER);
        btnSave.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSave.setBackground(getDrawable(R.drawable.bg_button_green_rounded));
        android.widget.LinearLayout.LayoutParams btnParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(48));
        btnSave.setLayoutParams(btnParams);
        layout.addView(btnSave);

        bottomSheet.setContentView(layout);

        // Load existing address into the field
        userRef.child("address").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String addr = snapshot.getValue(String.class);
                if (addr != null && !addr.isEmpty()) {
                    etAddress.setText(addr);
                    etAddress.setSelection(addr.length());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Save button click
        btnSave.setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                etAddress.setError("Please enter an address");
                return;
            }

            userRef.child("address").setValue(address).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update the label on the home screen
                    updateDeliverToLabel(address);
                    android.widget.Toast.makeText(this, "Address saved!", android.widget.Toast.LENGTH_SHORT).show();
                    bottomSheet.dismiss();
                } else {
                    android.widget.Toast.makeText(this, "Failed to save address", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });

        bottomSheet.show();
    }

    /**
     * Load the user's saved address and display it on the home header.
     */
    private void loadSavedAddress() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("address")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String addr = snapshot.getValue(String.class);
                        if (addr != null && !addr.isEmpty()) {
                            updateDeliverToLabel(addr);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * Updates the "Deliver to" label with a truncated version of the address.
     */
    private void updateDeliverToLabel(String address) {
        if (tvDeliverToLabel == null) return;
        String display = address.length() > 25
                ? address.substring(0, 25) + "…"
                : address;
        tvDeliverToLabel.setText("Deliver to: " + display);
    }

    /** Converts dp to pixels */
    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void setupProductCardClicks() {
        // Dairy products
        setupCardClick(R.id.card_product_yakult, "p1");
        setupCardClick(R.id.card_product_amul_milk, "p2");
        setupCardClick(R.id.card_product_epigamia, "p3");

        // Bread products
        setupCardClick(R.id.card_product_white_bread, "p4");
        setupCardClick(R.id.card_product_unibic_cookies, "p5");
        setupCardClick(R.id.card_product_elaichi_rusk, "p6");

        // Cold drinks
        setupCardClick(R.id.card_product_coca_cola, "p7");
        setupCardClick(R.id.card_product_mogu_mogu, "p8");
        setupCardClick(R.id.card_product_amul_kool, "p9");

        // Shop Now button
        findViewById(R.id.btn_shop_now).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
    }

    private void setupCardClick(int cardId, String productId) {
        try {
            findViewById(cardId).setOnClickListener(v -> {
                Intent intent = new Intent(this, ProductDetailActivity.class);
                intent.putExtra("product_id", productId);
                startActivity(intent);
            });
        } catch (Exception e) {
            // Card view might not exist, ignore
        }
    }

    private void updateCartBadge() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                TextView badge = findViewById(R.id.tv_cart_badge_count);
                if (badge != null) {
                    if (count > 0) {
                        badge.setVisibility(View.VISIBLE);
                        badge.setText(String.valueOf(count));
                    } else {
                        badge.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently fail
            }
        });
    }
}