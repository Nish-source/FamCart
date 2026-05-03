package com.example.testing.models;

import com.example.testing.models.CartItem;

import java.util.List;

public class Order {

    private String orderId;
    private List<com.example.testing.models.CartItem> items;
    private double totalAmount;
    private long timestamp;
    private String status;

    public Order() {
        // Required for Firebase
    }

    public Order(String orderId, List<com.example.testing.models.CartItem> items, double totalAmount, long timestamp, String status) {
        this.orderId = orderId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public List<com.example.testing.models.CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
