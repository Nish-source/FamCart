package com.example.testing.models;

import java.io.Serializable;

public class Address implements Serializable {
    private String addressId;
    private String tag; // Home, Work, etc.
    private String fullAddress;
    private boolean isDefault;

    public Address() {
    }

    public Address(String addressId, String tag, String fullAddress, boolean isDefault) {
        this.addressId = addressId;
        this.tag = tag;
        this.fullAddress = fullAddress;
        this.isDefault = isDefault;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
