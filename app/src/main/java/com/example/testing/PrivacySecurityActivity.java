package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PrivacySecurityActivity extends AppCompatActivity {

    private TextView tvStoredData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_security);

        tvStoredData = findViewById(R.id.tv_stored_data);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_logout).setOnClickListener(v -> logout());
        findViewById(R.id.btn_delete_account).setOnClickListener(v -> confirmDelete());

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String address = snapshot.child("address").getValue(String.class);
                String email = user.getEmail();

                StringBuilder sb = new StringBuilder();
                sb.append("Name: ").append(name != null ? name : "Not provided").append("\n");
                sb.append("Email: ").append(email).append("\n");
                sb.append("Phone: ").append(phone != null ? phone : "Not provided").append("\n");
                sb.append("Address: ").append(address != null ? address : "Not provided").append("\n\n");
                sb.append("User ID: ").append(user.getUid());

                tvStoredData.setText(sb.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure? This will permanently delete your orders and rewards.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        // Remove from DB first
        FirebaseDatabase.getInstance().getReference("users").child(uid).removeValue()
                .addOnCompleteListener(task -> {
                    // Then delete auth user
                    user.delete().addOnCompleteListener(authTask -> {
                        Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
                        logout();
                    });
                });
    }
}
