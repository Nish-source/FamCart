package com.example.testing;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.CartItem;
import com.example.testing.models.Product;
import com.example.testing.utils.ImageLoader;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvName, tvQuantity, tvPrice, tvOriginalPrice, tvDiscountBadge;
    private TextView tvRating, tvDescription, tvCategory;
    private TextView tvQtyCount;
    private TextView btnAddToCart;
    private ProgressBar progressBar;

    private Product currentProduct;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String productId = getIntent().getStringExtra("product_id");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading state while Firebase fetches
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        // ── STEP 1: Try to load from Firebase ──
        loadProductFromFirebase(productId);
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.iv_product_image);
        tvName = findViewById(R.id.tv_product_name);
        tvQuantity = findViewById(R.id.tv_product_quantity);
        tvPrice = findViewById(R.id.tv_price);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvDiscountBadge = findViewById(R.id.tv_discount_badge);
        tvRating = findViewById(R.id.tv_rating);
        tvDescription = findViewById(R.id.tv_description);
        tvCategory = findViewById(R.id.tv_category);
        tvQtyCount = findViewById(R.id.tv_qty_count);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        progressBar = findViewById(R.id.progress_bar); // optional — safe null check used below
    }

    private void loadProductFromFirebase(String productId) {
        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("Products")
                .child(productId);

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (snapshot.exists()) {
                    // ── Firebase product found ──
                    currentProduct = snapshot.getValue(Product.class);
                    if (currentProduct != null) {
                        currentProduct.setProductId(productId); // ensure ID is set
                        populateUI();
                        setupQuantityControls();
                        setupAddToCart();
                    } else {
                        fallbackToLocalProduct(productId);
                    }
                } else {
                    // ── Not in Firebase — try local data ──
                    fallbackToLocalProduct(productId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                // Network error — use local product
                fallbackToLocalProduct(productId);
            }
        });
    }

    /** Uses ProductDataProvider as a fallback when Firebase doesn't have the product. */
    private void fallbackToLocalProduct(String productId) {
        currentProduct = ProductDataProvider.getProductById(productId);
        if (currentProduct == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        populateUI();
        setupQuantityControls();
        setupAddToCart();
    }

    // ─────────────────────────────────────────────
    // UI Population
    // ─────────────────────────────────────────────

    private void populateUI() {
        tvName.setText(currentProduct.getName());
        tvQuantity.setText(currentProduct.getQuantity());
        tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", currentProduct.getPrice()));
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvDescription.setText(currentProduct.getDescription());
        tvCategory.setText(currentProduct.getCategory());

        // ── IMAGE: Use Glide for URL images, setImageResource for local ──
        ImageLoader.loadProduct(this, currentProduct, ivProductImage);

        // Original price with strikethrough
        if (currentProduct.getOriginalPrice() > currentProduct.getPrice()) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(String.format(Locale.getDefault(), "₹%.0f", currentProduct.getOriginalPrice()));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            int discount = (int) (((currentProduct.getOriginalPrice() - currentProduct.getPrice())
                    / currentProduct.getOriginalPrice()) * 100);
            tvDiscountBadge.setVisibility(View.VISIBLE);
            tvDiscountBadge.setText(discount + "% OFF");
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountBadge.setVisibility(View.GONE);
        }

        updateButtonText();
    }

    private void setupQuantityControls() {
        tvQtyCount.setText(String.valueOf(quantity));

        findViewById(R.id.btn_qty_plus).setOnClickListener(v -> {
            if (quantity < 10) {
                quantity++;
                tvQtyCount.setText(String.valueOf(quantity));
                updateButtonText();
            }
        });

        findViewById(R.id.btn_qty_minus).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQtyCount.setText(String.valueOf(quantity));
                updateButtonText();
            }
        });
    }

    private void updateButtonText() {
        double total = currentProduct.getPrice() * quantity;
        btnAddToCart.setText(String.format(Locale.getDefault(), "Add to Cart — ₹%.0f", total));
    }

    // ─────────────────────────────────────────────
    // Add to Cart
    // ─────────────────────────────────────────────

    private void setupAddToCart() {
        btnAddToCart.setOnClickListener(v -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAddToCart.setEnabled(false);

            String userId = auth.getCurrentUser().getUid();
            DatabaseReference cartRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("cart");

            // Check if product already exists in cart
            cartRef.orderByChild("productId").equalTo(currentProduct.getProductId())
                    .get().addOnCompleteListener(task -> {
                        btnAddToCart.setEnabled(true);

                        if (task.isSuccessful() && task.getResult().exists()) {
                            // Update existing item count
                            String existingKey = task.getResult().getChildren().iterator().next().getKey();
                            CartItem existingItem = task.getResult().getChildren().iterator().next()
                                    .getValue(CartItem.class);
                            if (existingItem != null && existingKey != null) {
                                int newCount = existingItem.getCount() + quantity;
                                cartRef.child(existingKey).child("count").setValue(newCount);
                                showCartSnackbar("Cart updated! (" + newCount + " in cart)");
                            }
                        } else {
                            // Add new item — use imageUrl if available
                            String key = cartRef.push().getKey();
                            CartItem cartItem;

                            if (currentProduct.hasImageUrl()) {
                                cartItem = new CartItem(
                                        currentProduct.getProductId(),
                                        currentProduct.getName(),
                                        currentProduct.getQuantity(),
                                        currentProduct.getPrice(),
                                        quantity,
                                        currentProduct.getImageUrl()   // ← Firebase URL
                                );
                            } else {
                                cartItem = new CartItem(
                                        currentProduct.getProductId(),
                                        currentProduct.getName(),
                                        currentProduct.getQuantity(),
                                        currentProduct.getPrice(),
                                        quantity,
                                        currentProduct.getDrawableResId()  // ← local drawable
                                );
                            }

                            cartItem.setCartItemId(key);
                            if (key != null) {
                                cartRef.child(key).setValue(cartItem);
                            }
                            showCartSnackbar("Added to cart!");
                        }
                    });
        });
    }

    private void showCartSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("View Cart", view ->
                startActivity(new Intent(ProductDetailActivity.this, CartActivity.class)));
        snackbar.setActionTextColor(0xFF22C55E);
        snackbar.setBackgroundTint(0xFF1F2937);
        snackbar.setTextColor(0xFFFFFFFF);
        snackbar.show();
    }
}
