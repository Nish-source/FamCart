package com.example.testing.models;

public class Product {

    private String productId;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private String quantity;
    private double price;
    private double originalPrice;
    private double rating;
    private int drawableResId;

    public Product() {

    }

    public Product(String productId, String name, String description, String category,
                   String quantity, double price, double originalPrice, double rating, int drawableResId) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.originalPrice = originalPrice;
        this.rating = rating;
        this.drawableResId = drawableResId;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getDrawableResId() { return drawableResId; }
    public void setDrawableResId(int drawableResId) { this.drawableResId = drawableResId; }

    public boolean hasImageUrl() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}