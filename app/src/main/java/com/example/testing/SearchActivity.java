package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.SearchAdapter;
import com.example.testing.models.Product;

import java.util.List;

public class SearchActivity extends AppCompatActivity implements SearchAdapter.OnProductClickListener {

    private EditText etSearch;
    private RecyclerView rvResults;
    private LinearLayout layoutEmptyState;
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        etSearch = findViewById(R.id.et_search);
        rvResults = findViewById(R.id.rv_search_results);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup RecyclerView
        adapter = new SearchAdapter(this);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        // Load all products initially
        List<Product> allProducts = ProductDataProvider.getAllProducts();
        adapter.updateProducts(allProducts);

        // Real-time search filtering
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Focus search input on open
        etSearch.requestFocus();
    }

    private void filterProducts(String query) {
        List<Product> filtered = ProductDataProvider.searchProducts(query);
        adapter.updateProducts(filtered);

        if (filtered.isEmpty() && !query.trim().isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getProductId());
        startActivity(intent);
    }
}
