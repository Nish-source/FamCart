package com.example.testing;

import com.example.testing.models.CartItem;
import com.example.testing.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CartManager {

    public interface CartCallback {
        void onSuccess(String message);
        void onFailure(Exception e);
    }

    /**
     * Adds a product to the cart or increments quantity if it already exists.
     * Uses productId as the key to prevent duplicates.
     */
    public static void addToCart(Product product, int quantity, CartCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            if (callback != null) callback.onFailure(new Exception("Please login first"));
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String productId = product.getProductId();
        
        if (productId == null || productId.isEmpty()) {
            if (callback != null) callback.onFailure(new Exception("Invalid Product ID"));
            return;
        }

        DatabaseReference cartItemRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart")
                .child(productId);

        cartItemRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    // Increment quantity
                    CartItem existingItem = snapshot.getValue(CartItem.class);
                    if (existingItem != null) {
                        int newCount = existingItem.getCount() + quantity;
                        cartItemRef.child("count").setValue(newCount)
                                .addOnSuccessListener(aVoid -> {
                                    if (callback != null) callback.onSuccess("Cart updated! (" + newCount + " in cart)");
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onFailure(e);
                                });
                    }
                } else {
                    // Create new cart item
                    CartItem newItem = new CartItem(
                            productId,
                            product.getName(),
                            product.getQuantity(),
                            product.getPrice(),
                            quantity,
                            product.getImageUrl()
                    );
                    newItem.setCartItemId(productId); // Using productId as cart item ID
                    
                    cartItemRef.setValue(newItem)
                            .addOnSuccessListener(aVoid -> {
                                if (callback != null) callback.onSuccess("Added to cart!");
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onFailure(e);
                            });
                }
            } else {
                if (callback != null) callback.onFailure(task.getException());
            }
        });
    }

    public static void updateQuantity(String productId, int newCount, CartCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference itemRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("cart")
                .child(productId);

        if (newCount <= 0) {
            itemRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful() && callback != null) callback.onSuccess("Item removed");
            });
        } else {
            itemRef.child("count").setValue(newCount).addOnCompleteListener(task -> {
                if (task.isSuccessful() && callback != null) callback.onSuccess("Quantity updated");
            });
        }
    }
}
