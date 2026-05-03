package com.example.testing.models;

import com.example.testing.models.CartItem;

import java.io.Serializable;
import java.util.List;

/**
 * Model representing an order as seen by the Admin panel.
 * Includes user details (name, address, phone) and payment info
 * so the admin can see complete order context.
 */
public class AdminOrder implements Serializable {

    private String orderId;
    private String userId;
    private String userName;
    private String userAddress;
    private String userPhone;
    private String paymentMethod;
    private List<com.example.testing.models.CartItem> items;
    private double totalAmount;
    private long timestamp;
    private String status;

    public AdminOrder() {
        // Required for Firebase
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<com.example.testing.models.CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
