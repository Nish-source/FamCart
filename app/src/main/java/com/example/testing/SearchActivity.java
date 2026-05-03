package com.example.testing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.SearchAdapter;
import com.example.testing.models.CartItem;
import com.example.testing.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private SearchAdapter adapter;

    private SharedPreferences prefs;
    private List<String> recentSearches = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupSearchInput();
        loadRecentSearches();
        populateIdleState();

        // Start in idle state
        showIdleState();

        // Focus search input on open
        etSearch.requestFocus();
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
    }

    private void setupRecyclerView() {
        adapter = new SearchAdapter(this);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Clear search text
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
        });

        // Clear all recent searches
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
                    filterProducts(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // On search action (keyboard enter)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                addToRecentSearches(query);
                hideKeyboard();
            }
            return true;
        });
    }

    // ─────────────────────────────────────────────
    // State Management
    // ─────────────────────────────────────────────

    private void showIdleState() {
        layoutIdleState.setVisibility(View.VISIBLE);
        layoutResultsState.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
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

    private void filterProducts(String query) {
        List<Product> filtered = ProductDataProvider.searchProducts(query);
        adapter.updateProducts(filtered);

        if (filtered.isEmpty()) {
            showEmptyState();
        } else {
            showResultsState(filtered.size());
        }
    }

    // ─────────────────────────────────────────────
    // Idle State UI — Recent + Popular chips
    // ─────────────────────────────────────────────

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

        // Split across two rows: row1 gets first 2, row2 gets next 3
        for (int i = 0; i < recentSearches.size() && i < 5; i++) {
            View chip = createRecentChip(recentSearches.get(i));
            if (i < 2) {
                rowRecent1.addView(chip);
            } else {
                rowRecent2.addView(chip);
            }
        }

        rowRecent2.setVisibility(rowRecent2.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void populatePopularChips() {
        rowPopular1.removeAllViews();
        rowPopular2.removeAllViews();

        // Row 1: first 3, Row 2: last 3
        for (int i = 0; i < POPULAR_SEARCHES.length; i++) {
            View chip = createPopularChip(POPULAR_SEARCHES[i]);
            if (i < 3) {
                rowPopular1.addView(chip);
            } else {
                rowPopular2.addView(chip);
            }
        }
    }

    /**
     * Creates a white pill chip with a clock icon — Figma "Recent Searches" style.
     * bg: white, rounded 20dp, subtle shadow, text: #4A5565, 13sp regular
     */
    private View createRecentChip(String text) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(getDrawable(R.drawable.bg_chip_recent));
        chip.setElevation(dpToPx(2));

        int hPad = dpToPx(14);
        int vPad = dpToPx(10);
        chip.setPadding(hPad, vPad, hPad, vPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(lp);

        // Clock icon
        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(12), dpToPx(12)));
        icon.setImageResource(R.drawable.ic_rewards);
        icon.setColorFilter(0xFF99A1AF);
        chip.addView(icon);

        // Text
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
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

    /**
     * Creates a green-tinted pill chip with a trending icon — Figma "Popular Searches" style.
     * bg: #F0FDF4, border: 10% green, text: #16A34A, 13sp medium
     */
    private View createPopularChip(String text) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackground(getDrawable(R.drawable.bg_chip_popular));

        int hPad = dpToPx(14);
        int vPad = dpToPx(10);
        chip.setPadding(hPad, vPad, hPad, vPad);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginEnd(dpToPx(8));
        lp.setMarginStart(0);
        chip.setLayoutParams(lp);

        // Trending icon
        ImageView icon = new ImageView(this);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(12), dpToPx(12)));
        icon.setImageResource(R.drawable.ic_arrow);
        icon.setColorFilter(0xFF16A34A);
        chip.addView(icon);

        // Text
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
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

    // ─────────────────────────────────────────────
    // Recent Searches Persistence (SharedPreferences)
    // ─────────────────────────────────────────────

    private void loadRecentSearches() {
        Set<String> saved = prefs.getStringSet(KEY_RECENT, null);
        recentSearches.clear();
        if (saved != null) {
            recentSearches.addAll(saved);
        }
        // Default items when no history exists
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
        // Remove duplicate then add at the start
        recentSearches.remove(query);
        recentSearches.add(0, query);
        // Keep max 5
        while (recentSearches.size() > 5) {
            recentSearches.remove(recentSearches.size() - 1);
        }
        saveRecentSearches();
        populateRecentChips();
    }

    // ─────────────────────────────────────────────
    // Adapter Callbacks
    // ─────────────────────────────────────────────

    @Override
    public void onProductClick(Product product) {
        // Save to recent searches
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

        // Create cart item
        CartItem item = new CartItem(
                product.getProductId(),
                product.getName(),
                product.getQuantity(),
                product.getPrice(),
                1,
                product.getDrawableResId()
        );

        cartRef.push().setValue(item).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
