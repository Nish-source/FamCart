package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.WishlistAdapter;
import com.example.testing.models.Product;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity implements WishlistAdapter.WishlistListener {

    private RecyclerView rvWishlist;
    private LinearLayout layoutEmpty;
    private TextView tvWishlistCount;
    private WishlistAdapter adapter;
    private List<Product> wishlistProducts = new ArrayList<>();
    private DatabaseReference wishlistRef;
    private ValueEventListener wishlistListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        rvWishlist = findViewById(R.id.rv_wishlist);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvWishlistCount = findViewById(R.id.tv_wishlist_count);

        adapter = new WishlistAdapter(this);
        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        rvWishlist.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        View btnBrowse = findViewById(R.id.btn_browse);
        if (btnBrowse != null) {
            btnBrowse.setOnClickListener(v -> finish());
        }

        loadWishlist();
    }

    private void loadWishlist() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        wishlistRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("wishlist");

        wishlistListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                wishlistProducts.clear();
                List<String> ids = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String productId = child.getValue(String.class);
                    if (productId != null) ids.add(productId);
                }
                
                if (ids.isEmpty()) {
                    updateUI();
                    return;
                }

                // Match against cached products
                List<Product> cached = ProductDataProvider.getCachedProducts();
                for (String id : ids) {
                    for (Product p : cached) {
                        if (p.getProductId().equals(id)) {
                            wishlistProducts.add(p);
                            break;
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WishlistActivity.this, "Failed to load wishlist", Toast.LENGTH_SHORT).show();
            }
        };
        wishlistRef.addValueEventListener(wishlistListener);
    }

    private void updateUI() {
        if (wishlistProducts.isEmpty()) {
            showEmptyState();
        } else {
            if (rvWishlist != null) rvWishlist.setVisibility(View.VISIBLE);
            if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
            if (tvWishlistCount != null) tvWishlistCount.setText(wishlistProducts.size() + " items");
            adapter.updateItems(wishlistProducts);
        }
    }

    private void showEmptyState() {
        if (rvWishlist != null) rvWishlist.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.VISIBLE);
        if (tvWishlistCount != null) tvWishlistCount.setText("");
    }

    @Override
    public void onRemoveFromWishlist(Product product) {
        if (wishlistRef != null) {
            wishlistRef.orderByValue().equalTo(product.getProductId())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DataSnapshot child : task.getResult().getChildren()) {
                                child.getRef().removeValue();
                            }
                            Snackbar.make(rvWishlist != null ? rvWishlist : findViewById(android.R.id.content), 
                                    "Removed from wishlist", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onAddToCart(Product product) {
        CartManager.addToCart(product, 1, new CartManager.CartCallback() {
            @Override
            public void onSuccess(String message) {
                showCartSnackbar(message);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(WishlistActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCartSnackbar(String message) {
        View anchor = rvWishlist != null ? rvWishlist : findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(anchor, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("View Cart", v ->
                startActivity(new Intent(this, CartActivity.class)));
        snackbar.setActionTextColor(0xFF22C55E);
        snackbar.setBackgroundTint(0xFF1F2937);
        snackbar.setTextColor(0xFFFFFFFF);
        snackbar.show();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getProductId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wishlistRef != null && wishlistListener != null) {
            wishlistRef.removeEventListener(wishlistListener);
        }
    }

    public static void toggleWishlist(String productId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("wishlist");

        ref.orderByValue().equalTo(productId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot child : task.getResult().getChildren()) {
                    child.getRef().removeValue();
                }
            } else {
                ref.push().setValue(productId);
            }
        });
    }

    public static void isInWishlist(String productId, WishlistCheckCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            callback.onResult(false);
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("wishlist")
                .orderByValue().equalTo(productId)
                .get().addOnCompleteListener(task -> {
                    boolean exists = task.isSuccessful() && task.getResult().exists();
                    callback.onResult(exists);
                });
    }

    public interface WishlistCheckCallback {
        void onResult(boolean isInWishlist);
    }
}