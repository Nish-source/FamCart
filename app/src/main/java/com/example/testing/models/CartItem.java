package com.example.testing.models;

import java.io.Serializable;
<<<<<<< HEAD
=======

public class CartItem implements Serializable {
>>>>>>> 5ca5e1075dd70c549c30ca34e25cc36adec93a17

public class CartItem implements Serializable {

    public String cartItemId;
    public String productId;
    public String productName;
    public String productQuantity;
    public double productPrice;
    public int count;
    public String imageUrl;
    public double totalPrice; // Added for Firebase mapping
    public int drawableResId; // Added for Firebase mapping

    public CartItem() {
        // Required for Firebase
    }

    public CartItem(String productId, String productName, String productQuantity,
                    double productPrice, int count, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.count = count;
        this.imageUrl = imageUrl;
        this.totalPrice = productPrice * count;
    }

    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductQuantity() { return productQuantity; }
    public void setProductQuantity(String productQuantity) { this.productQuantity = productQuantity; }

    public double getProductPrice() { return productPrice; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getTotalPrice() {
        return productPrice * count;
    }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public int getDrawableResId() { return drawableResId; }
    public void setDrawableResId(int drawableResId) { this.drawableResId = drawableResId; }
}
