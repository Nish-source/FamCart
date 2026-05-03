package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderPlacedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_placed);

        // Handle back press — go to home instead of back to checkout
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });

        // Get data from intent
        String orderId = getIntent().getStringExtra("order_id");
        double totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        String address = getIntent().getStringExtra("address");

        // Set order ID
        TextView tvOrderId = findViewById(R.id.tv_order_id);
        if (tvOrderId != null && orderId != null && !orderId.isEmpty()) {
            // Show last 5 chars as readable ID
            String shortId = "#FRC-" + orderId.substring(Math.max(0, orderId.length() - 5)).toUpperCase();
            tvOrderId.setText(shortId);
        }

        // Set delivery address
        TextView tvAddress = findViewById(R.id.tv_delivery_address);
        if (tvAddress != null && address != null && !address.isEmpty()) {
            tvAddress.setText(address);
        }

        // Set order placed time
        TextView tvTimePlaced = findViewById(R.id.tv_time_placed);
        if (tvTimePlaced != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            tvTimePlaced.setText(sdf.format(new Date()));
        }

        // View Details → go to Orders
        findViewById(R.id.btn_view_details).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
        });

        // View Orders button
        findViewById(R.id.btn_view_orders).setOnClickListener(v -> {
            startActivity(new Intent(this, OrdersActivity.class));
            finish();
        });

        // Back to Home button
        findViewById(R.id.btn_back_to_home).setOnClickListener(v -> {
            navigateToHome();
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
