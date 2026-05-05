package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.CategoryAdapter;
import com.example.testing.adapters.HomeProductAdapter;
import com.example.testing.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView tvDeliverToLabel;
    private ProgressBar progressBar;
    private View scrollViewContent;
    private CategoryAdapter categoryAdapter;
    private HomeProductAdapter dairyAdapter, breadAdapter, coldDrinksAdapter;
    private com.example.testing.adapters.OfferAdapter offersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Fix Top White Space / Status Bar overlap
        View rootView = findViewById(R.id.layout_root);
        if (rootView != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, systemBars.top, 0, 0);
                return insets;
            });
        }

        tvDeliverToLabel = findViewById(R.id.tv_deliver_to_label);
        progressBar = findViewById(R.id.progress_bar);
        scrollViewContent = findViewById(R.id.scroll_view_content);

        setupRecyclerViews();
        setupBottomNav();
        setupSearchBar();
        setupHeaderClicks();
        updateCartBadge();
        loadSavedAddress();
        fetchProducts();
    }

    private void setupRecyclerViews() {
        // Categories
        RecyclerView rvCategories = findViewById(R.id.rv_categories);
        if (rvCategories != null) {
            categoryAdapter = new CategoryAdapter(category -> {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("category", category);
                startActivity(intent);
            });
            rvCategories.setAdapter(categoryAdapter);
        }

        // Products
        RecyclerView rvDairy = findViewById(R.id.rv_dairy);
        if (rvDairy != null) {
            dairyAdapter = createHomeProductAdapter();
            rvDairy.setAdapter(dairyAdapter);
        }

        RecyclerView rvBread = findViewById(R.id.rv_bread);
        if (rvBread != null) {
            breadAdapter = createHomeProductAdapter();
            rvBread.setAdapter(breadAdapter);
        }

        RecyclerView rvColdDrinks = findViewById(R.id.rv_cold_drinks);
        if (rvColdDrinks != null) {
            coldDrinksAdapter = createHomeProductAdapter();
            rvColdDrinks.setAdapter(coldDrinksAdapter);
        }

        RecyclerView rvOffers = findViewById(R.id.rv_offers);
        if (rvOffers != null) {
            offersAdapter = new com.example.testing.adapters.OfferAdapter(product -> {
                Intent intent = new Intent(this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getProductId());
                startActivity(intent);
            });
            offersAdapter.setOnOfferActionListener(new com.example.testing.adapters.OfferAdapter.OnOfferActionListener() {
                @Override
                public void onAddToCart(Product product) {
                    CartManager.addToCart(product, 1, new CartManager.CartCallback() {
                        @Override
                        public void onSuccess(String message) {
                            android.widget.Toast.makeText(MainActivity.this, message, android.widget.Toast.LENGTH_SHORT).show();
                            updateCartBadge();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            android.widget.Toast.makeText(MainActivity.this, e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onOrderNow(Product product) {
                    CartManager.addToCart(product, 1, new CartManager.CartCallback() {
                        @Override
                        public void onSuccess(String message) {
                            startActivity(new Intent(MainActivity.this, CartActivity.class));
                        }
                        @Override
                        public void onFailure(Exception e) {
                            android.widget.Toast.makeText(MainActivity.this, e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            rvOffers.setAdapter(offersAdapter);
        }
    }

    private HomeProductAdapter createHomeProductAdapter() {
        HomeProductAdapter adapter = new HomeProductAdapter(product -> {
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getProductId());
            startActivity(intent);
        });
        
        adapter.setOnAddToCartListener(product -> {
            CartManager.addToCart(product, 1, new CartManager.CartCallback() {
                @Override
                public void onSuccess(String message) {
                    android.widget.Toast.makeText(MainActivity.this, message, android.widget.Toast.LENGTH_SHORT).show();
                    updateCartBadge();
                }
                @Override
                public void onFailure(Exception e) {
                    android.widget.Toast.makeText(MainActivity.this, e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        return adapter;
    }

    private void fetchProducts() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ProductDataProvider.fetchAllProducts(new ProductDataProvider.ProductFetchListener() {
            @Override
            public void onProductsFetched(java.util.List<Product> allProducts) {
                // Fetch categories
                ProductDataProvider.fetchAllCategories(new ProductDataProvider.CategoryFetchListener() {
                    @Override
                    public void onCategoriesFetched(java.util.List<com.example.testing.models.Category> categories) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (scrollViewContent != null) scrollViewContent.setVisibility(View.VISIBLE);

                        if (categoryAdapter != null) categoryAdapter.updateCategories(categories);

                        java.util.List<Product> dairyList = new java.util.ArrayList<>();
                        java.util.List<Product> bakeryList = new java.util.ArrayList<>();
                        java.util.List<Product> juiceList = new java.util.ArrayList<>();
                        java.util.List<Product> offerList = new java.util.ArrayList<>();

                        for (Product p : allProducts) {
                            if (p.getCategory() == null) continue;
                            String cat = p.getCategory().toLowerCase();

                            if (cat.contains("dairy") || cat.contains("milk") || cat.contains("egg")) {
                                dairyList.add(p);
                            } else if (cat.contains("bakery") || cat.contains("bread")) {
                                bakeryList.add(p);
                            } else if (cat.contains("juice") || cat.contains("beverage") || cat.contains("drink")) {
                                juiceList.add(p);
                            }

                            if (p.getOriginalPrice() > p.getPrice()) {
                                offerList.add(p);
                            }
                        }

                        if (dairyAdapter != null) dairyAdapter.updateProducts(dairyList);
                        if (breadAdapter != null) breadAdapter.updateProducts(bakeryList);
                        if (coldDrinksAdapter != null) coldDrinksAdapter.updateProducts(juiceList);
                        
                        if (offersAdapter != null) {
                            if (offerList.isEmpty() && allProducts.size() > 5) {
                                offersAdapter.updateProducts(allProducts.subList(0, 5));
                            } else {
                                offersAdapter.updateProducts(offerList);
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (scrollViewContent != null) scrollViewContent.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                android.widget.Toast.makeText(MainActivity.this, "Data Error: " + error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        loadSavedAddress();
        loadProfileImage();
    }

    private void loadProfileImage() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid())
                .child("profileImage")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String url = snapshot.getValue(String.class);
                        android.widget.ImageView iv = findViewById(R.id.btn_profile_avatar);
                        if (url != null && !url.isEmpty() && iv != null) {
                            com.bumptech.glide.Glide.with(MainActivity.this)
                                    .load(url)
                                    .placeholder(R.drawable.ic_account_circle2)
                                    .into(iv);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupBottomNav() {
        findViewById(R.id.btn_tab_home).setOnClickListener(v -> {
            if (scrollViewContent != null) scrollViewContent.scrollTo(0, 0);
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
        // "See All" buttons
        View btnCatSeeAll = findViewById(R.id.btn_categories_see_all);
        if (btnCatSeeAll != null) btnCatSeeAll.setOnClickListener(v -> navigateToCategory("All"));

        View btnOffSeeAll = findViewById(R.id.btn_offers_see_all);
        if (btnOffSeeAll != null) btnOffSeeAll.setOnClickListener(v -> navigateToCategory("Offers"));

        View btnDairySeeAll = findViewById(R.id.btn_dairy_see_all);
        if (btnDairySeeAll != null) btnDairySeeAll.setOnClickListener(v -> navigateToCategory("Dairy"));

        View btnBreadSeeAll = findViewById(R.id.btn_bread_see_all);
        if (btnBreadSeeAll != null) btnBreadSeeAll.setOnClickListener(v -> navigateToCategory("Bakery"));

        View btnColdSeeAll = findViewById(R.id.btn_cold_drinks_see_all);
        if (btnColdSeeAll != null) btnColdSeeAll.setOnClickListener(v -> navigateToCategory("Juices"));

        // Profile avatar
        try {
            View btnProfile = findViewById(R.id.btn_profile_avatar);
            if (btnProfile != null) btnProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, ProfileActivity.class));
            });
        } catch (Exception ignored) {}

        // Notification bell
        try {
            View btnNotify = findViewById(R.id.btn_notifications);
            if (btnNotify != null) btnNotify.setOnClickListener(v -> {
                startActivity(new Intent(this, NotificationsActivity.class));
            });
        } catch (Exception ignored) {}

        // Address selector
        try {
            View btnAddress = findViewById(R.id.btn_address_selector);
            if (btnAddress != null) btnAddress.setOnClickListener(v -> {
                showAddressBottomSheet();
            });
        } catch (Exception ignored) {}
    }

    private void navigateToCategory(String category) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private void showAddressBottomSheet() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            android.widget.Toast.makeText(this, "Please login first", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId);

        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheet =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(24), dp(24), dp(32));
        layout.setBackgroundColor(0xFFFFFFFF);

        TextView title = new TextView(this);
        title.setText("Delivery Address");
        title.setTextSize(18);
        title.setTextColor(0xFF101828);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Enter or update your delivery address");
        subtitle.setTextSize(13);
        subtitle.setTextColor(0xFF99A1AF);
        subtitle.setPadding(0, dp(4), 0, dp(16));
        layout.addView(subtitle);

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

        View spacer = new View(this);
        spacer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, dp(16)));
        layout.addView(spacer);

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

        btnSave.setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                etAddress.setError("Please enter an address");
                return;
            }

            userRef.child("address").setValue(address).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
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

    private void updateDeliverToLabel(String address) {
        if (tvDeliverToLabel == null) return;
        String display = address.length() > 25
                ? address.substring(0, 25) + "…"
                : address;
        tvDeliverToLabel.setText("Deliver to: " + display);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}