package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
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

        // Task 1: Fix Top White Space / Status Bar overlap
        View rootView = findViewById(R.id.layout_root);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

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
        categoryAdapter = new CategoryAdapter(category -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("category", category);
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);

        // Products
        RecyclerView rvDairy = findViewById(R.id.rv_dairy);
        dairyAdapter = createHomeProductAdapter();
        rvDairy.setAdapter(dairyAdapter);

        RecyclerView rvBread = findViewById(R.id.rv_bread);
        breadAdapter = createHomeProductAdapter();
        rvBread.setAdapter(breadAdapter);

        RecyclerView rvColdDrinks = findViewById(R.id.rv_cold_drinks);
        coldDrinksAdapter = createHomeProductAdapter();
        rvColdDrinks.setAdapter(coldDrinksAdapter);

        RecyclerView rvOffers = findViewById(R.id.rv_offers);
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
        progressBar.setVisibility(View.VISIBLE);
        // Do NOT hide scroll view if we already have some data (optional, but better UX)
        // scrollViewContent.setVisibility(View.GONE);

        ProductDataProvider.fetchAllProducts(new ProductDataProvider.ProductFetchListener() {
            @Override
            public void onProductsFetched(java.util.List<Product> allProducts) {
                android.util.Log.d("DATA_CHECK", "Total products fetched: " + allProducts.size());
                
                // Now fetch categories
                ProductDataProvider.fetchAllCategories(new ProductDataProvider.CategoryFetchListener() {
                    @Override
                    public void onCategoriesFetched(java.util.List<com.example.testing.models.Category> categories) {
                        progressBar.setVisibility(View.GONE);
                        scrollViewContent.setVisibility(View.VISIBLE);

                        // 1. Update categories
                        categoryAdapter.updateCategories(categories);

                        // 2. Filter products for sections
                        java.util.List<Product> dairyList = new java.util.ArrayList<>();
                        java.util.List<Product> bakeryList = new java.util.ArrayList<>();
                        java.util.List<Product> juiceList = new java.util.ArrayList<>();
                        java.util.List<Product> offerList = new java.util.ArrayList<>();

                        for (Product p : allProducts) {
                            if (p.getCategory() == null) continue;
                            String cat = p.getCategory().toLowerCase();

                            // Logic to map categories to sections
                            if (cat.contains("dairy") || cat.contains("milk") || cat.contains("egg")) {
                                dairyList.add(p);
                            } else if (cat.contains("bakery") || cat.contains("bread")) {
                                bakeryList.add(p);
                            } else if (cat.contains("juice") || cat.contains("beverage") || cat.contains("drink")) {
                                juiceList.add(p);
                            }

                            // Logic for offers (items with discounts)
                            if (p.getOriginalPrice() > p.getPrice()) {
                                offerList.add(p);
                            }
                        }

                        // 3. Update adapters
                        dairyAdapter.updateProducts(dairyList);
                        breadAdapter.updateProducts(bakeryList);
                        coldDrinksAdapter.updateProducts(juiceList);
                        
                        // Failsafe for offers: show some products if no discounts found
                        if (offerList.isEmpty() && allProducts.size() > 5) {
                            offersAdapter.updateProducts(allProducts.subList(0, 5));
                        } else {
                            offersAdapter.updateProducts(offerList);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("DATA_CHECK", "Category fetch error: " + error);
                        progressBar.setVisibility(View.GONE);
                        scrollViewContent.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("DATA_CHECK", "Product fetch error: " + error);
                progressBar.setVisibility(View.GONE);
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
        // Task 2: "See All" button functionality
        findViewById(R.id.btn_categories_see_all).setOnClickListener(v -> navigateToCategory("All"));
        findViewById(R.id.btn_offers_see_all).setOnClickListener(v -> navigateToCategory("Offers"));
        findViewById(R.id.btn_dairy_see_all).setOnClickListener(v -> navigateToCategory("Dairy"));
        findViewById(R.id.btn_bread_see_all).setOnClickListener(v -> navigateToCategory("Bakery"));
        findViewById(R.id.btn_cold_drinks_see_all).setOnClickListener(v -> navigateToCategory("Juices"));

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

    private void navigateToCategory(String category) {
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Silently fail
            }
        });
    }
}