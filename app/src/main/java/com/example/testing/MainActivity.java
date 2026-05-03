package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupBottomNav();
        setupSearchBar();
        setupProductCardClicks();
        setupHeaderClicks();
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }

    private void setupBottomNav() {
        // Home tab - already on home
        findViewById(R.id.btn_tab_home).setOnClickListener(v -> {

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
    }

    private void openSearchWithQuery(String query) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra("search_query", query);
        startActivity(intent);
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

        findViewById(R.id.btn_dairy_see_all).setOnClickListener(v -> {
            openSearchWithQuery("Dairy & Milk");
        });

        findViewById(R.id.btn_bread_see_all).setOnClickListener(v -> {
            openSearchWithQuery("Bread & Bakery");
        });

        findViewById(R.id.btn_cold_drinks_see_all).setOnClickListener(v -> {
            openSearchWithQuery("Cold Drinks & Juices");
        });

        findViewById(R.id.btn_offers_see_all).setOnClickListener(v -> {
            openSearchWithQuery("Snacks");
        });

        findViewById(R.id.btn_categories_see_all).setOnClickListener(v -> {
            openSearchWithQuery("");
        });

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
                android.widget.TextView badge = findViewById(R.id.tv_cart_badge_count);
                if (badge != null) {
                    if (count > 0) {
                        badge.setVisibility(View.VISIBLE);
                        badge.setText(String.valueOf(count));
                    } else {
                        badge.setVisibility(View.GONE);
                    }
                }
            }
            private void openSearchWithQuery(String query) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("search_query", query);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}