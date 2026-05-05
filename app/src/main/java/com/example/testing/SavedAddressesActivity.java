package com.example.testing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.AddressAdapter;
import com.example.testing.models.Address;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedAddressesActivity extends AppCompatActivity implements AddressAdapter.OnAddressActionListener {

    private RecyclerView rvAddresses;
    private LinearLayout layoutEmpty;
    private AddressAdapter adapter;
    private DatabaseReference addressRef;
    private ValueEventListener addressListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);

        rvAddresses = findViewById(R.id.rv_addresses);
        layoutEmpty = findViewById(R.id.layout_empty);
        ExtendedFloatingActionButton fabAdd = findViewById(R.id.fab_add_address);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new AddressAdapter(this);
        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            // Open Add Address Bottom Sheet or Activity
            showAddAddressDialog(null);
        });

        loadAddresses();
    }

    private void loadAddresses() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        addressRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("addresses");

        addressListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Address> addressList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Address address = child.getValue(Address.class);
                    if (address != null) {
                        address.setAddressId(child.getKey());
                        addressList.add(address);
                    }
                }

                if (addressList.isEmpty()) {
                    showEmptyState();
                } else {
                    rvAddresses.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.updateItems(addressList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedAddressesActivity.this, "Failed to load addresses", Toast.LENGTH_SHORT).show();
            }
        };
        addressRef.addValueEventListener(addressListener);
    }

    private void showEmptyState() {
        rvAddresses.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showAddAddressDialog(Address existingAddress) {
        // Simplified: using an activity for adding/editing address
        Intent intent = new Intent(this, AddEditAddressActivity.class);
        if (existingAddress != null) {
            intent.putExtra("address", existingAddress);
        }
        startActivity(intent);
    }

    @Override
    public void onEdit(Address address) {
        showAddAddressDialog(address);
    }

    @Override
    public void onDelete(Address address) {
        if (addressRef != null && address.getAddressId() != null) {
            addressRef.child(address.getAddressId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Address deleted", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onSetDefault(Address address) {
        if (addressRef != null) {
            // Unset other defaults first (simplified approach)
            addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (Boolean.TRUE.equals(child.child("isDefault").getValue(Boolean.class))) {
                            child.getRef().child("isDefault").setValue(false);
                        }
                    }
                    // Set this one as default
                    addressRef.child(address.getAddressId()).child("isDefault").setValue(true);
                    
                    // Also update the main profile address for legacy compatibility
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("address").setValue(address.getFullAddress());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addressRef != null && addressListener != null) {
            addressRef.removeEventListener(addressListener);
        }
    }
}
