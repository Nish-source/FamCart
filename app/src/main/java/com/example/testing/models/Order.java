package com.example.testing.models;

import java.util.List;

public class Order {

    public String orderId;
    public String userId;
    public String userName;
    public String address;
    public String phone;
    public String paymentMethod;
    public List<CartItem> items;
    public double totalAmount;
    public long timestamp;
    public String status;

    public Order() {
        // Required for Firebase
    }

    public Order(String orderId, String userId, String userName, String address, String phone, 
                 String paymentMethod, List<CartItem> items, double totalAmount, long timestamp, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.address = address;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.items = items;
        this.totalAmount = totalAmount;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters (keeping them for compatibility)
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
