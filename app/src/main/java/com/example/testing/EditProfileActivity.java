package com.example.testing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.famcart.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private EditText etName, etEmail, etPhone, etAddress;
    private TextView btnSave;
    private ImageView ivProfileImage;
    private DatabaseReference userRef;
    private Uri imageUri;
    private String userId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        btnSave = findViewById(R.id.btn_save);
        ivProfileImage = findViewById(R.id.iv_profile_image);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        View btnChangePhoto = findViewById(R.id.btn_change_photo);
        if (btnChangePhoto != null) {
            btnChangePhoto.setOnClickListener(v -> openGallery());
        }
        
        btnSave.setOnClickListener(v -> saveProfile());

        loadCurrentProfile();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (ivProfileImage != null) {
                Glide.with(this).load(imageUri).into(ivProfileImage);
            }
        }
    }

    private void loadCurrentProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = user.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        final String email = user.getEmail();
        if (email != null) {
            etEmail.setText(email);
        }

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) etName.setText(name);

                    String phone = snapshot.child("phone").getValue(String.class);
                    if (phone != null && !phone.isEmpty()) {
                        etPhone.setText(phone);
                    } else if (email != null && email.endsWith("@famcart.com")) {
                        String extractedPhone = email.replace("@famcart.com", "");
                        if (extractedPhone.matches("[0-9]{10}")) {
                            etPhone.setText(extractedPhone);
                        }
                    }

                    String address = snapshot.child("address").getValue(String.class);
                    if (address != null) etAddress.setText(address);

                    String profileImageUrl = snapshot.child("profileImage").getValue(String.class);
                    if (profileImageUrl != null && !profileImageUrl.isEmpty() && ivProfileImage != null) {
                        Glide.with(EditProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_account_circle2)
                                .error(R.drawable.ic_account_circle2)
                                .into(ivProfileImage);
                    }
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

        progressDialog.show();

        if (imageUri != null) {
            uploadImageAndSave(name, phone, address);
        } else {
            updateUserDatabase(name, phone, address, null);
        }
    }

    private void uploadImageAndSave(String name, String phone, String address) {
        if (userId == null || imageUri == null) {
            progressDialog.dismiss();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("profile_images").child(userId + ".jpg");

        progressDialog.setMessage("Uploading image...");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateUserDatabase(name, phone, address, uri.toString());
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, "Failed to get URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfileActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploading: " + (int) progress + "%");
                });
    }

    private void updateUserDatabase(String name, String phone, String address, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (!phone.isEmpty()) updates.put("phone", phone);
        if (!address.isEmpty()) updates.put("address", address);
        if (imageUrl != null) {
            updates.put("profileImage", imageUrl);
        }

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}