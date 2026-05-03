package com.example.testing;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;
import com.example.testing.models.Address;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddEditAddressActivity extends AppCompatActivity {

    private EditText etTag, etFullAddress;
    private CheckBox cbDefault;
    private Address existingAddress;
    private DatabaseReference addressRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_address);

        etTag = findViewById(R.id.et_tag);
        etFullAddress = findViewById(R.id.et_full_address);
        cbDefault = findViewById(R.id.cb_default);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView btnSave = findViewById(R.id.btn_save);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            addressRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("addresses");
        }

        existingAddress = (Address) getIntent().getSerializableExtra("address");
        if (existingAddress != null) {
            tvTitle.setText("Edit Address");
            etTag.setText(existingAddress.getTag());
            etFullAddress.setText(existingAddress.getFullAddress());
            cbDefault.setChecked(existingAddress.isDefault());
        }

        btnSave.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String tag = etTag.getText().toString().trim();
        String fullAddress = etFullAddress.getText().toString().trim();
        boolean isDefault = cbDefault.isChecked();

        if (tag.isEmpty()) {
            etTag.setError("Required");
            return;
        }
        if (fullAddress.isEmpty()) {
            etFullAddress.setError("Required");
            return;
        }

        if (addressRef == null) return;

        String id = existingAddress != null ? existingAddress.getAddressId() : addressRef.push().getKey();
        if (id == null) return;

        Address address = new Address(id, tag, fullAddress, isDefault);

        if (isDefault) {
            // Unset other defaults
            addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (!child.getKey().equals(id) && Boolean.TRUE.equals(child.child("isDefault").getValue(Boolean.class))) {
                            child.getRef().child("isDefault").setValue(false);
                        }
                    }
                    performSave(id, address);
                    
                    // Also update main profile address
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("address").setValue(fullAddress);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    performSave(id, address);
                }
            });
        } else {
            performSave(id, address);
        }
    }

    private void performSave(String id, Address address) {
        addressRef.child(id).setValue(address).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
