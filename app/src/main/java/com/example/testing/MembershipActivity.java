package com.example.testing;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MembershipActivity extends AppCompatActivity {

    private TextView btnJoinGold, tvStatus, btnRevoke;
    private DatabaseReference membershipRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        btnJoinGold = findViewById(R.id.btn_join_gold);
        tvStatus = findViewById(R.id.tv_membership_status);
        btnRevoke = findViewById(R.id.btn_revoke_membership);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            membershipRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("membership");

            checkStatus();
        }

        btnJoinGold.setOnClickListener(v -> joinGold());
        btnRevoke.setOnClickListener(v -> revokeMembership());
    }

    private void checkStatus() {
        membershipRef.child("isActive").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isActive = snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (isActive) {
                    btnJoinGold.setText("Membership Active");
                    btnJoinGold.setEnabled(false);
                    btnJoinGold.setAlpha(0.7f);
                    tvStatus.setVisibility(View.VISIBLE);
                    tvStatus.setText("You are a Gold Member until next month!");
                    btnRevoke.setVisibility(View.VISIBLE);
                } else {
                    btnJoinGold.setText("Join Gold for ₹99/month");
                    btnJoinGold.setEnabled(true);
                    btnJoinGold.setAlpha(1.0f);
                    tvStatus.setVisibility(View.GONE);
                    btnRevoke.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void joinGold() {
        if (membershipRef == null) return;

        membershipRef.child("isActive").setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Welcome to FamCart Gold!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to join. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void revokeMembership() {
        if (membershipRef == null) return;

        membershipRef.child("isActive").setValue(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Membership cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to cancel. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
