package com.example.testing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.SearchAdapter;
import com.example.testing.models.CartItem;
import com.example.testing.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnProductClickListener {

    private static final String PREFS_NAME = "search_prefs";
    private static final String KEY_RECENT = "recent_searches";

    // Popular search terms (hardcoded from Figma design)
    private static final String[] POPULAR_SEARCHES = {
            "Instant Oats", "Paneer", "Maggi Noodles",
            "Rice", "Pasta", "Buns"
    };

    private EditText etSearch;
    private ImageView btnClearSearch;
    private RecyclerView rvResults;
    private TextView tvResultsCount;
    private View layoutIdleState, layoutResultsState, layoutEmptyState;
    private LinearLayout layoutRecentHeader;
    private LinearLayout rowRecent1, rowRecent2;
    private LinearLayout rowPopular1, rowPopular2;
    private ProgressBar progressBar;
    private SearchAdapter adapter;

    private SharedPreferences prefs;
    private List<String> recentSearches = new ArrayList<>();

    private List<Product> allFirebaseProducts = new ArrayList<>();
    private boolean isFirebaseLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("FamCartCRASH", "CRASH: " + throwable.getMessage(), throwable);
        });

        setContentView(R.layout.activity_search);

        try {
            prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            initViews();
            setupRecyclerView();
            setupClickListeners();
            setupSearchInput();
            loadRecentSearches();
            populateIdleState();
            showIdleState();
            prefetchFirebaseProducts();
            etSearch.requestFocus();
        } catch (Exception e) {
            Log.e("FamCartCRASH", "SearchActivity crashed: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        rvResults = findViewById(R.id.rv_search_results);
        tvResultsCount = findViewById(R.id.tv_results_count);
        layoutIdleState = findViewById(R.id.layout_idle_state);
        layoutResultsState = findViewById(R.id.layout_results_state);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        layoutRecentHeader = findViewById(R.id.layout_recent_header);
        rowRecent1 = findViewById(R.id.row_recent_1);
        rowRecent2 = findViewById(R.id.row_recent_2);
        rowPopular1 = findViewById(R.id.row_popular_1);
        rowPopular2 = findViewById(R.id.row_popular_2);

        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        adapter = new SearchAdapter(this);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
        });

        findViewById(R.id.btn_clear_recent).setOnClickListener(v -> {
            recentSearches.clear();
            saveRecentSearches();
            populateRecentChips();
        });
    }

    private void setupSearchInput() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showIdleState();
                    btnClearSearch.setVisibility(View.GONE);
                } else {
                    btnClearSearch.setVisibility(View.VISIBLE);
                    searchProductsFromFirebase(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                addToRecentSearches(query);
                hideKeyboard();
            }
            return true;
        });
    }

    private void prefetchFirebaseProducts() {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Products");
        Log.d("FamCartDebug", "Starting Firebase fetch from: " + productsRef.toString());


        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FamCartDebug", "Firebase responded. Snapshot exists: " + snapshot.exists());
                Log.d("FamCartDebug", "Total products fetched: " + snapshot.getChildrenCount());
                allFirebaseProducts.clear();

                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    Product product = productSnap.getValue(Product.class);
                    if (product != null) {
                        if (product.getProductId() == null || product.getProductId().isEmpty()) {
                            product.setProductId(productSnap.getKey());
                        }
                        allFirebaseProducts.add(product);
                        Log.d("FamCartDebug", "Loaded: " + product.getName() + " | imageUrl: " + product.getImageUrl());
                    }
                }
                isFirebaseLoaded = true;

                String currentQuery = etSearch.getText().toString().trim();
                if (!currentQuery.isEmpty()) {
                    searchProductsFromFirebase(currentQuery);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FamCartDebug", "Firebase FAILED: " + error.getMessage() + " | Code: " + error.getCode());
                Toast.makeText(SearchActivity.this,
                        "Could not load products from server. Showing local data.",
                        Toast.LENGTH_SHORT).show();
                isFirebaseLoaded = true;
                ProductDataProvider.loadProducts(products -> {

                    allFirebaseProducts.clear();
                    allFirebaseProducts.addAll(products);

                    populateIdleState();
                });
            }
        });
    }
    
    
    private void searchProductsFromFirebase(String query) {
        if (!isFirebaseLoaded) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            return;
        }
        if (progressBar != null) progressBar.setVisibility(View.GONE);

        String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();
        List<Product> results = new ArrayList<>();

        for (Product p : allFirebaseProducts) {

            if (p == null) continue;

            String name = p.getName() != null ? p.getName().toLowerCase(Locale.getDefault()) : "";

            String category = p.getCategory() != null
                    ? p.getCategory().toLowerCase(Locale.getDefault())
                    : "";

            String description = p.getDescription() != null
                    ? p.getDescription().toLowerCase(Locale.getDefault())
                    : "";

            if (name.contains(lowerQuery) ||
                    category.contains(lowerQuery) ||
                    description.contains(lowerQuery)) {

                results.add(p);
            }
        }

        adapter.updateProducts(results);

        if (results.isEmpty()) {
            showEmptyState();
        } else {
            showResultsState(results.size());
        }
    }

    

    private void showIdleState() {
        layoutIdleState.setVisibility(View.VISIBLE);
        layoutResultsState.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void showResultsState(int count) {
        layoutIdleState.setVisibility(View.GONE);
        layoutResultsState.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        tvResultsCount.setText(String.format(Locale.getDefault(), "%d results found", count));
    }

    private void showEmptyState() {
        layoutIdleState.setVisibility(View.GONE);
        layoutResultsState.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
    }



    @Override
    public void onProductClick(Product product) {
        addToRecentSearches(product.getName());

        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getProductId());
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");


        CartItem item;
        if (product.hasImageUrl()) {
            item = new CartItem(
                    product.getProductId(),
                    product.getName(),
                    product.getQuantity(),
                    product.getPrice(),
                    1,
                    product.getImageUrl()   
            );
        } else {
            item = new CartItem(
                    product.getProductId(),
                    product.getName(),
                    product.getQuantity(),
                    product.getPrice(),
                    1,
                    product.getDrawableResId()  
            );
        }

        // Check if product already in cart to avoid duplicates
        cartRef.orderByChild("productId").equalTo(product.getProductId())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        // Product already in cart — increment count
                        String existingKey = task.getResult().getChildren().iterator().next().getKey();
                        CartItem existing = task.getResult().getChildren().iterator().next().getValue(CartItem.class);
                        if (existing != null && existingKey != null) {
                            int newCount = existing.getCount() + 1;
                            cartRef.child(existingKey).child("count").setValue(newCount);
                            Toast.makeText(this, "Cart updated!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // New item — add to cart
                        cartRef.push().setValue(item).addOnCompleteListener(addTask -> {
                            if (addTask.isSuccessful()) {
                                Toast.makeText(this, product.getName() + " added to cart",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to add to cart",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

   
    private void populateIdleState() {
        populateRecentChips();
        populatePopularChips();
    }

    private void populateRecentChips() {
        rowRecent1.removeAllViews();
        rowRecent2.removeAllViews();

        if (recentSearches.isEmpty()) {
            layoutRecentHeader.setVisibility(View.GONE);
            rowRecent1.setVisibility(View.GONE);
            rowRecent2.setVisibility(View.GONE);
            return;
        }

        layoutRecentHeader.setVisibility(View.VISIBLE);
        rowRecent1.setVisibility(View.VISIBLE);

        for (int i = 0; i < recentSearches.size() && i < 5; i++) {
            View chip = createRecentChip(recentSearches.get(i));
            if (i < 2) rowRecent1.addView(chip);
            else rowRecent2.addView(chip);
        }
        rowRecent2.setVisibility(rowRecent2.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void populatePopularChips() {
        rowPopular1.removeAllViews();
        rowPopular2.removeAllViews();
        for (int i = 0; i < POPULAR_SEARCHES.length; i++) {
            View chip = createPopularChip(POPULAR_SEARCHES[i]);
            if (i < 3) rowPopular1.addView(chip);
            else rowPopular2.addView(chip);
        }
    }

    private View createRecentChip(String text) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(getDrawable(R.drawable.bg_chip_recent));
        chip.setElevation(dpToPx(2));
        int hPad = dpToPx(14), vPad = dpToPx(10);
        chip.setPadding(hPad, vPad, hPad, vPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(lp);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(12), dpToPx(12)));
        icon.setImageResource(R.drawable.ic_rewards);
        icon.setColorFilter(0xFF99A1AF);
        chip.addView(icon);

        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLp.setMarginStart(dpToPx(6));
        tv.setLayoutParams(tvLp);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setTextColor(0xFF4A5565);
        tv.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        chip.addView(tv);

        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setOnClickListener(v -> {
            etSearch.setText(text);
            etSearch.setSelection(text.length());
        });
        return chip;
    }

    private View createPopularChip(String text) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(getDrawable(R.drawable.bg_chip_popular));
        int hPad = dpToPx(14), vPad = dpToPx(10);
        chip.setPadding(hPad, vPad, hPad, vPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(lp);

        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(12), dpToPx(12)));
        icon.setImageResource(R.drawable.ic_arrow);
        icon.setColorFilter(0xFF16A34A);
        chip.addView(icon);

        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLp.setMarginStart(dpToPx(6));
        tv.setLayoutParams(tvLp);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setTextColor(0xFF16A34A);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        chip.addView(tv);

        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setOnClickListener(v -> {
            etSearch.setText(text);
            etSearch.setSelection(text.length());
        });
        return chip;
    }
    

    private void loadRecentSearches() {
        Set<String> saved = prefs.getStringSet(KEY_RECENT, null);
        recentSearches.clear();
        if (saved != null) {
            recentSearches.addAll(saved);
        }
        if (recentSearches.isEmpty()) {
            recentSearches.addAll(Arrays.asList(
                    "Organic Milk", "Britannia Cake",
                    "Whole Wheat Bread", "Eggs", "Coffee"));
        }
    }

    private void saveRecentSearches() {
        prefs.edit().putStringSet(KEY_RECENT, new HashSet<>(recentSearches)).apply();
    }

    private void addToRecentSearches(String query) {
        recentSearches.remove(query);
        recentSearches.add(0, query);
        while (recentSearches.size() > 5) recentSearches.remove(recentSearches.size() - 1);
        saveRecentSearches();
        populateRecentChips();
    }


    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
