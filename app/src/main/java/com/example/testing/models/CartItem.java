package com.example.testing.models;

public class CartItem {

    private String cartItemId;
    private String productId;
    private String productName;
    private String productQuantity;
    private double productPrice;
    private int count;
    private int drawableResId;

    public CartItem() {
        // Required for Firebase
    }

    public CartItem(String productId, String productName, String productQuantity,
                    double productPrice, int count, int drawableResId) {
        this.productId = productId;
        this.productName = productName;
        this.productQuantity = productQuantity;
        this.productPrice = productPrice;
        this.count = count;
        this.drawableResId = drawableResId;
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

    public int getDrawableResId() { return drawableResId; }
    public void setDrawableResId(int drawableResId) { this.drawableResId = drawableResId; }

    public double getTotalPrice() {
        return productPrice * count;
    }
}
