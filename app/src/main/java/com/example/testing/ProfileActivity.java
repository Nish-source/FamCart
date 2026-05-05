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

        setupMenuItem(R.id.menu_my_orders, "My Orders", "Track and view past orders", R.drawable.orders_button);
        setupMenuItem(R.id.menu_addresses, "Saved Addresses", "Manage delivery addresses", R.drawable.map_button);
        setupMenuItem(R.id.menu_payments, "Payment Methods", "Cards, UPI & wallets", R.drawable.ic_payment);
        setupMenuItem(R.id.menu_wishlist, "Wishlist", "Items you've saved", R.drawable.ic_wishlist);

        setupMenuItem(R.id.menu_refer, "Refer & Earn", "Get ₹50 for every referral", R.drawable.ic_refer);

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

        findViewById(R.id.menu_wishlist).setOnClickListener(v ->
                startActivity(new Intent(this, WishlistActivity.class)));

        findViewById(R.id.menu_refer).setOnClickListener(v ->

        findViewById(R.id.menu_rewards).setOnClickListener(v ->

        findViewById(R.id.menu_notifications).setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));

        findViewById(R.id.menu_app_settings).setOnClickListener(v ->

        findViewById(R.id.menu_help).setOnClickListener(v ->

        findViewById(R.id.menu_privacy).setOnClickListener(v ->

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
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String email = user.getEmail();
        if (email != null) {
            tvUserEmail.setText(email);
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);

                String phone = snapshot.child("phone").getValue(String.class);
                if (phone != null && phone.matches("[0-9]{10}")) {
                    tvUserPhone.setText("+91 " + phone.substring(0, 5) + " " + phone.substring(5));
                    tvUserPhone.setVisibility(View.VISIBLE);
                }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadStats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
