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
import com.example.testing.adapters.CartAdapter;
import com.example.testing.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private LinearLayout layoutBottomBar;
    private TextView tvTotalPrice, tvItemCount, btnCheckout;

    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private DatabaseReference cartRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadCart();
    }

    private void initViews() {
        rvCartItems = findViewById(R.id.rv_cart_items);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        layoutBottomBar = findViewById(R.id.layout_bottom_bar);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvItemCount = findViewById(R.id.tv_item_count);
        btnCheckout = findViewById(R.id.btn_checkout);
    }

    private void setupRecyclerView() {
        adapter = new CartAdapter(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_browse_products).setOnClickListener(v -> {
            finish(); // Go back to home
        });

        btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        });
    }

    private void loadCart() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        cartRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart");

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    CartItem item = child.getValue(CartItem.class);
                    if (item != null) {
                        item.setCartItemId(child.getKey());
                        cartItems.add(item);
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Failed to load cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if (cartItems.isEmpty()) {
            showEmptyState();
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            layoutEmptyCart.setVisibility(View.GONE);
            layoutBottomBar.setVisibility(View.VISIBLE);
            adapter.updateItems(cartItems);

            // Calculate total
            double total = 0;
            for (CartItem item : cartItems) {
                total += item.getTotalPrice();
            }
            tvTotalPrice.setText(String.format(Locale.getDefault(), "₹%.0f", total));
            tvItemCount.setText(cartItems.size() + " items");
        }
    }

    private void showEmptyState() {
        rvCartItems.setVisibility(View.GONE);
        layoutEmptyCart.setVisibility(View.VISIBLE);
        layoutBottomBar.setVisibility(View.GONE);
    }

    @Override
    public void onQuantityChanged(CartItem item, int newCount) {
        if (cartRef != null && item.getCartItemId() != null) {
            cartRef.child(item.getCartItemId()).child("count").setValue(newCount);
        }
    }

    @Override
    public void onItemRemoved(CartItem item) {
        if (cartRef != null && item.getCartItemId() != null) {
            cartRef.child(item.getCartItemId()).removeValue();
        }
    }
}
