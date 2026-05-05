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

import com.bumptech.glide.Glide;
import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private TextView tvOrdersCount, tvSavedAmount, tvPoints;
    private ImageView ivAvatar;

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
        loadUserData();
        loadStats();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvUserPhone = findViewById(R.id.tv_user_phone);
        tvOrdersCount = findViewById(R.id.tv_orders_count);
        tvSavedAmount = findViewById(R.id.tv_saved_amount);
        tvPoints = findViewById(R.id.tv_points);
        ivAvatar = findViewById(R.id.iv_avatar);

        setupMenuItem(R.id.menu_my_orders, "My Orders", "Track and view past orders", R.drawable.orders_button);
        setupMenuItem(R.id.menu_addresses, "Saved Addresses", "Manage delivery addresses", R.drawable.map_button);
        setupMenuItem(R.id.menu_payments, "Payment Methods", "Cards, UPI & wallets", R.drawable.ic_payment);
        setupMenuItem(R.id.menu_wishlist, "Wishlist", "Items you've saved", R.drawable.ic_wishlist);

        setupMenuItem(R.id.menu_refer, "Refer & Earn", "Get ₹50 for every referral", R.drawable.ic_refer);
<<<<<<< HEAD
        setupMenuItem(R.id.menu_rewards, "FamCart Rewards", "Points available", R.drawable.ic_rewards);
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

        setupMenuItem(R.id.menu_notifications, "Notifications", "Order & promo alerts", R.drawable.ic_notif_bell);
        setupMenuItem(R.id.menu_app_settings, "App Settings", "Language, theme & more", R.drawable.ic_settings);
        setupMenuItem(R.id.menu_help, "Help & Support", "FAQs and contact us", R.drawable.ic_help);
        setupMenuItem(R.id.menu_privacy, "Privacy & Security", "Data and account safety", R.drawable.ic_privacy);
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
            ivIcon.setImageTintList(null);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.menu_my_orders).setOnClickListener(v ->
                startActivity(new Intent(this, OrdersActivity.class)));

        findViewById(R.id.menu_addresses).setOnClickListener(v ->
<<<<<<< HEAD
                startActivity(new Intent(this, SavedAddressesActivity.class)));

        findViewById(R.id.menu_payments).setOnClickListener(v ->
                Toast.makeText(this, "Payment methods can be managed during checkout", Toast.LENGTH_SHORT).show());

=======

>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        findViewById(R.id.menu_wishlist).setOnClickListener(v ->
                startActivity(new Intent(this, WishlistActivity.class)));

        findViewById(R.id.menu_refer).setOnClickListener(v ->
<<<<<<< HEAD
                startActivity(new Intent(this, ReferEarnActivity.class)));

        findViewById(R.id.menu_rewards).setOnClickListener(v ->
                startActivity(new Intent(this, RewardsActivity.class)));
=======

        findViewById(R.id.menu_rewards).setOnClickListener(v ->
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

        findViewById(R.id.menu_notifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        findViewById(R.id.menu_app_settings).setOnClickListener(v ->

        findViewById(R.id.menu_help).setOnClickListener(v ->

        findViewById(R.id.menu_privacy).setOnClickListener(v ->

<<<<<<< HEAD
        View btnGold = findViewById(R.id.layout_gold_membership);
        if (btnGold != null) {
            btnGold.setOnClickListener(v -> startActivity(new Intent(this, MembershipActivity.class)));
        }

=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
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

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.btn_tab_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
<<<<<<< HEAD
        findViewById(R.id.btn_tab_search).setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        findViewById(R.id.btn_tab_cart).setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        findViewById(R.id.btn_tab_orders).setOnClickListener(v -> startActivity(new Intent(this, OrdersActivity.class)));
        findViewById(R.id.btn_tab_profile).setOnClickListener(v -> { });
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
<<<<<<< HEAD
        if (user == null) {
            if (tvUserName != null) tvUserName.setText("Guest User");
            if (tvUserEmail != null) tvUserEmail.setText("Please login to continue");
            return;
        }
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

        final String email = user.getEmail();
        if (email != null && tvUserEmail != null) {
            tvUserEmail.setText(email);
        }

<<<<<<< HEAD
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
<<<<<<< HEAD
                if (tvUserName != null) {
                    if (name != null && !name.isEmpty()) {
                        tvUserName.setText(name);
                    } else {
                        String fallbackName = email != null ? email.split("@")[0] : "FamCart User";
                        tvUserName.setText(fallbackName);
                    }
                }
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

                String phone = snapshot.child("phone").getValue(String.class);
                if (phone == null && email != null && email.endsWith("@famcart.com")) {
                    phone = email.replace("@famcart.com", "");
                }

                if (phone != null && phone.matches("[0-9]{10}") && tvUserPhone != null) {
                    tvUserPhone.setText("+91 " + phone.substring(0, 5) + " " + phone.substring(5));
                    tvUserPhone.setVisibility(View.VISIBLE);
                }

<<<<<<< HEAD
                String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                if (profileImageUrl != null && !profileImageUrl.isEmpty() && ivAvatar != null) {
                    Glide.with(ProfileActivity.this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_account_circle2)
                            .into(ivAvatar);
=======
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
                }
                
                updateWishlistSubtitle(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
<<<<<<< HEAD
        if (user == null) {
            if (tvOrdersCount != null) tvOrdersCount.setText("0");
            if (tvSavedAmount != null) tvSavedAmount.setText("₹0");
            if (tvPoints != null) tvPoints.setText("0");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        userRef.child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
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

                if (tvOrdersCount != null) tvOrdersCount.setText(String.valueOf(orderCount));
                if (tvSavedAmount != null) tvSavedAmount.setText(String.format(Locale.getDefault(), "₹%.0f", totalSpent * 0.05));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        userRef.child("rewards").child("coins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coins = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                if (tvPoints != null) tvPoints.setText(String.valueOf(coins));
                
                View rewardsItem = findViewById(R.id.menu_rewards);
                if (rewardsItem != null) {
                    TextView tvSub = rewardsItem.findViewById(R.id.tv_menu_subtitle);
                    if (tvSub != null) {
                        tvSub.setText(coins + " points available");
                    }
                }
            }
=======


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            }

            }
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}