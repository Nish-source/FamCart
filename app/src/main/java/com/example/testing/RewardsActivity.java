package com.example.testing;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class RewardsActivity extends AppCompatActivity {

    private TextView tvTotalCoins, tvCoinsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        tvTotalCoins = findViewById(R.id.tv_total_coins);
        tvCoinsValue = findViewById(R.id.tv_coins_value);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadRewards();
    }

    private void loadRewards() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(auth.getCurrentUser().getUid())
                .child("rewards")
                .child("coins");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int coins = 0;
                if (snapshot.exists()) {
                    Integer val = snapshot.getValue(Integer.class);
                    if (val != null) coins = val;
                }
                tvTotalCoins.setText(String.valueOf(coins));
                tvCoinsValue.setText(String.format(Locale.getDefault(), "Worth ₹%.2f", coins / 10.0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
