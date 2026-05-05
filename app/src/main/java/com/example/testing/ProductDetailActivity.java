package com.example.testing;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.CartItem;
import com.example.testing.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            currentProduct = ProductDataProvider.getProductById(productId);
        }

        if (currentProduct == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateUI();
        setupQuantityControls();
        setupAddToCart();
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
        tvName.setText(currentProduct.getName());
        tvQuantity.setText(currentProduct.getQuantity());
        tvPrice.setText(String.format(Locale.getDefault(), "₹%.0f", currentProduct.getPrice()));
        tvRating.setText(String.valueOf(currentProduct.getRating()));
        tvDescription.setText(currentProduct.getDescription());
        tvCategory.setText(currentProduct.getCategory());

        if (currentProduct.getDrawableResId() != 0) {
            ivProductImage.setImageResource(currentProduct.getDrawableResId());
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
        double total = currentProduct.getPrice() * quantity;
        btnAddToCart.setText(String.format(Locale.getDefault(), "Add to Cart — ₹%.0f", total));
    }

    private void setupAddToCart() {
        btnAddToCart.setOnClickListener(v -> {
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
