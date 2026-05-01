package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private TextView tvOrdersCount, tvSavedAmount, tvPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupClickListeners();
        loadUserData();
        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh stats when returning from Orders etc.
        loadStats();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserPhone = findViewById(R.id.tv_user_phone);
        tvOrdersCount = findViewById(R.id.tv_orders_count);
        tvSavedAmount = findViewById(R.id.tv_saved_amount);
        tvPoints = findViewById(R.id.tv_points);

        // Section: MY ACCOUNT — with proper icons matching Figma design
        setupMenuItem(R.id.menu_my_orders, "My Orders", "Track and view past orders", R.drawable.orders_button);
        setupMenuItem(R.id.menu_addresses, "Saved Addresses", "Manage delivery addresses", R.drawable.map_button);
        setupMenuItem(R.id.menu_payments, "Payment Methods", "Cards, UPI & wallets", R.drawable.ic_payment);
        setupMenuItem(R.id.menu_wishlist, "Wishlist", "Items you've saved", R.drawable.ic_wishlist);

        // Section: REWARDS & OFFERS
        setupMenuItem(R.id.menu_refer, "Refer & Earn", "Get ₹50 for every referral", R.drawable.ic_refer);
        setupMenuItem(R.id.menu_rewards, "FamCart Rewards", "240 points available", R.drawable.ic_rewards);

        // Section: SETTINGS
        setupMenuItem(R.id.menu_notifications, "Notifications", "Order & promo alerts", R.drawable.ic_notif_bell);
        setupMenuItem(R.id.menu_app_settings, "App Settings", "Language, theme & more", R.drawable.ic_settings);
        setupMenuItem(R.id.menu_help, "Help & Support", "FAQs and contact us", R.drawable.ic_help);
        setupMenuItem(R.id.menu_privacy, "Privacy & Security", "Data and account safety", R.drawable.ic_privacy);

        // Set proper edit icon
        ImageView btnEditProfile = findViewById(R.id.btn_edit_profile);
        if (btnEditProfile != null) {
            btnEditProfile.setImageResource(R.drawable.ic_edit);
            btnEditProfile.setAlpha(1.0f);
        }
    }

    private void setupMenuItem(int viewId, String title, String subtitle, int iconRes) {
        View view = findViewById(viewId);
        if (view == null) return;

        TextView tvTitle = view.findViewById(R.id.tv_menu_title);
        TextView tvSub = view.findViewById(R.id.tv_menu_subtitle);
        ImageView ivIcon = view.findViewById(R.id.iv_menu_icon);

        if (tvTitle != null) tvTitle.setText(title);
        if (tvSub != null) tvSub.setText(subtitle);
        if (ivIcon != null && iconRes != 0) {
            ivIcon.setImageResource(iconRes);
            // Remove tint so each icon's own color is used
            ivIcon.setImageTintList(null);
        }
    }

    private void setupClickListeners() {
        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Edit profile
        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile — Coming soon", Toast.LENGTH_SHORT).show());

        // My Orders
        findViewById(R.id.menu_my_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
        });

        // Other menu items placeholder
        View.OnClickListener comingSoon = v -> Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show();
        findViewById(R.id.menu_addresses).setOnClickListener(comingSoon);
        findViewById(R.id.menu_payments).setOnClickListener(comingSoon);
        findViewById(R.id.menu_wishlist).setOnClickListener(comingSoon);
        findViewById(R.id.menu_refer).setOnClickListener(comingSoon);
        findViewById(R.id.menu_rewards).setOnClickListener(comingSoon);
        findViewById(R.id.menu_notifications).setOnClickListener(comingSoon);
        findViewById(R.id.menu_app_settings).setOnClickListener(comingSoon);
        findViewById(R.id.menu_help).setOnClickListener(comingSoon);
        findViewById(R.id.menu_privacy).setOnClickListener(comingSoon);

        // Logout with confirmation dialog
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log Out", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Bottom nav
        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.btn_tab_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        findViewById(R.id.btn_tab_search).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
        findViewById(R.id.btn_tab_cart).setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });
        findViewById(R.id.btn_tab_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
        });
        // Profile tab - already here
        findViewById(R.id.btn_tab_profile).setOnClickListener(v -> { });
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvUserName.setText("Guest User");
            tvUserEmail.setText("Please login to continue");
            return;
        }

        String email = user.getEmail();
        if (email != null) {
            tvUserEmail.setText(email);

            // Extract phone from email (format: phone@famcart.com)
            if (email.endsWith("@famcart.com")) {
                String phone = email.replace("@famcart.com", "");
                if (phone.matches("[0-9]{10}")) {
                    tvUserPhone.setText("+91 " + phone.substring(0, 5) + " " + phone.substring(5));
                    tvUserPhone.setVisibility(android.view.View.VISIBLE);
                }
            }
        }

        // Try to load name from Firebase DB
        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Load name
                String name = snapshot.child("name").getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    tvUserName.setText(name);
                } else {
                    // Fallback: use email prefix
                    String fallbackName = email != null ? email.split("@")[0] : "FamCart User";
                    tvUserName.setText(fallbackName);
                }

                // Also check if phone is stored
                String phone = snapshot.child("phone").getValue(String.class);
                if (phone != null && phone.matches("[0-9]{10}")) {
                    tvUserPhone.setText("+91 " + phone.substring(0, 5) + " " + phone.substring(5));
                    tvUserPhone.setVisibility(android.view.View.VISIBLE);
                }

                // Update rewards subtitle with actual points
                View rewardsItem = findViewById(R.id.menu_rewards);
                if (rewardsItem != null) {
                    TextView tvSub = rewardsItem.findViewById(R.id.tv_menu_subtitle);
                    if (tvSub != null && tvPoints != null) {
                        String pts = tvPoints.getText().toString();
                        tvSub.setText(pts + " points available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tvOrdersCount.setText("0");
            tvSavedAmount.setText("₹0");
            tvPoints.setText("0");
            return;
        }

        String userId = user.getUid();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long orderCount = snapshot.getChildrenCount();
                double totalSpent = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    Double amount = child.child("totalAmount").getValue(Double.class);
                    if (amount != null) {
                        totalSpent += amount;
                    }
                }

                tvOrdersCount.setText(String.valueOf(orderCount));
                tvSavedAmount.setText(String.format(Locale.getDefault(), "₹%.0f", totalSpent * 0.05));
                int points = (int) (totalSpent * 0.5);
                tvPoints.setText(String.valueOf(points));

                // Update rewards subtitle
                View rewardsItem = findViewById(R.id.menu_rewards);
                if (rewardsItem != null) {
                    TextView tvSub = rewardsItem.findViewById(R.id.tv_menu_subtitle);
                    if (tvSub != null) {
                        tvSub.setText(points + " points available");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
