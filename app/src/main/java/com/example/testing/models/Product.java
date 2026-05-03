package com.example.testing.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class Product {

    public String id; // Added for Firebase compatibility
    public String productId;
    public String name;
    public String brand;
    public String description;
    public String category;
    public String imageUrl;
    public String quantity;
    public double price;
    public double originalPrice;
    public double rating;
    public int stockQuantity;

    public Product() {
        // Required for Firebase
    }

    public Product(String productId, String name, String brand, String description, String category,
                   String quantity, double price, double originalPrice, double rating, int stockQuantity) {
        this.productId = productId;
        this.id = productId;
        this.name = name;
        this.brand = brand;
        this.description = description;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.originalPrice = originalPrice;
        this.rating = rating;
        this.stockQuantity = stockQuantity;
    }

    @PropertyName("id")
    public String getId() { return id != null ? id : productId; }
    @PropertyName("id")
    public void setId(String id) { this.id = id; this.productId = id; }

    public String getProductId() { return productId != null ? productId : id; }
    public void setProductId(String productId) { this.productId = productId; this.id = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

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

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}
