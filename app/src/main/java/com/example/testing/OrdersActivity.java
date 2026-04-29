package com.example.testing;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.OrderAdapter;
import com.example.testing.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private LinearLayout layoutEmptyOrders;
    private ProgressBar progressBar;
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        rvOrders = findViewById(R.id.rv_orders);
        layoutEmptyOrders = findViewById(R.id.layout_empty_orders);
        progressBar = findViewById(R.id.progress_bar);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup RecyclerView
        adapter = new OrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                List<Order> orders = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Order order = child.getValue(Order.class);
                    if (order != null) {
                        order.setOrderId(child.getKey());
                        orders.add(order);
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(orders, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                if (orders.isEmpty()) {
                    showEmptyState();
                } else {
                    rvOrders.setVisibility(View.VISIBLE);
                    layoutEmptyOrders.setVisibility(View.GONE);
                    adapter.updateOrders(orders);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(OrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        rvOrders.setVisibility(View.GONE);
        layoutEmptyOrders.setVisibility(View.VISIBLE);
    }
}
