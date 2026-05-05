package com.example.testing;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.famcart.R;
import com.example.testing.models.Product;
<<<<<<< HEAD
import com.google.android.material.snackbar.Snackbar;
=======
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvName, tvQuantity, tvPrice, tvOriginalPrice, tvDiscountBadge;
    private TextView tvRating, tvDescription, tvCategory;
    private TextView tvQtyCount;
    private TextView btnAddToCart;

    private Product currentProduct;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Get product
        String productId = getIntent().getStringExtra("product_id");
        if (productId != null) {
            ProductDataProvider.getProductById(productId, new ProductDataProvider.ProductFetchListener() {
                @Override
                public void onProductsFetched(java.util.List<Product> products) {
                    if (!products.isEmpty()) {
                        currentProduct = products.get(0);
                        populateUI();
                        setupQuantityControls();
                        setupAddToCart();
                        setupWishlist();
                    } else {
                        handleProductNotFound();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ProductDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            handleProductNotFound();
        }
    }

<<<<<<< HEAD
    private void handleProductNotFound() {
        Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
        finish();
=======
        populateUI();
        setupQuantityControls();
        setupAddToCart();
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
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
    }

    private void populateUI() {
        if (currentProduct == null) return;
        tvName.setText(currentProduct.getName());
        tvQuantity.setText(currentProduct.getQuantity());
        tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", currentProduct.getPrice()));
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvDescription.setText(currentProduct.getDescription());
        tvCategory.setText(currentProduct.getCategory());

        if (currentProduct.getImageUrl() != null && !currentProduct.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentProduct.getImageUrl())
                    .placeholder(R.drawable.essentials)
                    .into(ivProductImage);
        } else {
            ivProductImage.setImageResource(R.drawable.essentials);
        }

        // Original price with strikethrough
        if (currentProduct.getOriginalPrice() > currentProduct.getPrice()) {
            tvOriginalPrice.setVisibility(View.VISIBLE);
            tvOriginalPrice.setText(String.format(Locale.getDefault(), "₹%.0f", currentProduct.getOriginalPrice()));
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // Calculate discount percentage
            int discount = (int) (((currentProduct.getOriginalPrice() - currentProduct.getPrice()) / currentProduct.getOriginalPrice()) * 100);
            tvDiscountBadge.setVisibility(View.VISIBLE);
            tvDiscountBadge.setText(discount + "% OFF");
        } else {
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountBadge.setVisibility(View.GONE);
        }

        updateButtonText();
    }

    private void setupQuantityControls() {
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
        if (currentProduct == null) return;
        double total = currentProduct.getPrice() * quantity;
        btnAddToCart.setText(String.format(Locale.getDefault(), "Add to Cart — ₹%.0f", total));
    }

    private void setupAddToCart() {
        btnAddToCart.setOnClickListener(v -> {
<<<<<<< HEAD
            if (currentProduct == null) return;
            // Disable button briefly to prevent double-taps
            btnAddToCart.setEnabled(false);

            CartManager.addToCart(currentProduct, quantity, new CartManager.CartCallback() {
                @Override
                public void onSuccess(String message) {
                    btnAddToCart.setEnabled(true);
                    showCartSnackbar(message);
                }

                @Override
                public void onFailure(Exception e) {
                    btnAddToCart.setEnabled(true);
                    Toast.makeText(ProductDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showCartSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("View Cart", view -> {
            startActivity(new Intent(ProductDetailActivity.this, CartActivity.class));
        });
        snackbar.setActionTextColor(0xFF22C55E); // Brand green
        snackbar.setBackgroundTint(0xFF1F2937);   // Dark slate
        snackbar.setTextColor(0xFFFFFFFF);         // White
        snackbar.show();
    }

    private void setupWishlist() {
        if (btnWishlist == null || currentProduct == null) return;

        // Check current wishlist state
        WishlistActivity.isInWishlist(currentProduct.getProductId(), inWishlist -> {
            isWishlisted = inWishlist;
            updateWishlistIcon();
        });

        btnWishlist.setOnClickListener(v -> {
            isWishlisted = !isWishlisted;
            updateWishlistIcon();
            WishlistActivity.toggleWishlist(currentProduct.getProductId());

            View rootView = findViewById(android.R.id.content);
            if (isWishlisted) {
                Snackbar.make(rootView, "Added to wishlist ❤", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(0xFF1F2937)
                        .setTextColor(0xFFFFFFFF)
                        .show();
            } else {
                Snackbar.make(rootView, "Removed from wishlist", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(0xFF1F2937)
                        .setTextColor(0xFFFFFFFF)
                        .show();
            }
        });
    }

    private void updateWishlistIcon() {
        if (btnWishlist == null) return;
        if (isWishlisted) {
            btnWishlist.setColorFilter(0xFFEF4444); // Red filled
        } else {
            btnWishlist.setColorFilter(0xFF99A1AF); // Grey outline
        }
    }
}
=======
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

            // Check if product already exists in cart
            cartRef.orderByChild("productId").equalTo(currentProduct.getProductId())
                    .get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            // Update existing item
                            String existingKey = task.getResult().getChildren().iterator().next().getKey();
                            CartItem existingItem = task.getResult().getChildren().iterator().next().getValue(CartItem.class);
                            if (existingItem != null && existingKey != null) {
                                int newCount = existingItem.getCount() + quantity;
                                cartRef.child(existingKey).child("count").setValue(newCount);
                                Toast.makeText(this, "Cart updated!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Add new item
                            String key = cartRef.push().getKey();
                            CartItem cartItem = new CartItem(
                                    currentProduct.getProductId(),
                                    currentProduct.getName(),
                                    currentProduct.getQuantity(),
                                    currentProduct.getPrice(),
                                    quantity,
                                    currentProduct.getDrawableResId()
                            );
                            cartItem.setCartItemId(key);
                            if (key != null) {
                                cartRef.child(key).setValue(cartItem);
                            }
                            Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    });
        });
    }
}
>>>>>>> e24a567d8ac8039753a386af752c39232bc39929
