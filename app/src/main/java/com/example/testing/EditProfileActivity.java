package com.example.testing;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etAddress;
    private TextView btnSave;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnSave = findViewById(R.id.btn_save);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProfile());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set email (read-only)
        String email = user.getEmail();
        if (email != null) {
            etEmail.setText(email);
        }

        String userId = user.getUid();
        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Pre-fill name
                String name = snapshot.child("name").getValue(String.class);
                if (name != null && !name.isEmpty()) {
                    etName.setText(name);
                }

                // Pre-fill phone
                String phone = snapshot.child("phone").getValue(String.class);
                if (phone != null && !phone.isEmpty()) {
                    etPhone.setText(phone);
                } else if (email != null && email.endsWith("@famcart.com")) {
                    // Extract phone from email format
                    String extractedPhone = email.replace("@famcart.com", "");
                    if (extractedPhone.matches("[0-9]{10}")) {
                        etPhone.setText(extractedPhone);
                    }
                }

                // Pre-fill address
                String address = snapshot.child("address").getValue(String.class);
                if (address != null && !address.isEmpty()) {
                    etAddress.setText(address);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validate
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (!phone.isEmpty() && !phone.matches("[0-9]{10}")) {
            etPhone.setError("Enter a valid 10-digit number");
            etPhone.requestFocus();
            return;
        }

        if (userRef == null) {
            Toast.makeText(this, "Error: not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // Build update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (!phone.isEmpty()) {
            updates.put("phone", phone);
        }
        if (!address.isEmpty()) {
            updates.put("address", address);
        }

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            btnSave.setEnabled(true);
            btnSave.setText("Save Changes");

            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
