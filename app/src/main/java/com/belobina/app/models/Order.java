package com.belobina.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Order {
    private String id;
    private String orderID;
    private String assID;
    private String userID;
    private String customerId;
    private String customerName;
    private String furnitureName;
    private String furnitureImageUrl;
    private int quantity;
    private double orderAmount;
    private boolean orderAssemblyReq;
    private String assemblerName;
    private String orderStatus;
    private Timestamp orderDate;
    private Timestamp completionDate;
    private String deliveryAddress;

    public Order() {}

    public String getId() { return id; }
    public void setId(String id) {
        this.id = id;
        if (this.orderID == null) {
            this.orderID = id;
        }
    }
    public String getOrderID() { return orderID != null ? orderID : id; }
    public void setOrderID(String orderID) {
        this.orderID = orderID;
        if (this.id == null) {
            this.id = orderID;
        }
    }
    public String getAssID() { return assID; }
    public void setAssID(String assID) { this.assID = assID; }
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }
    @Exclude
    public String getCustomerId() { return userID != null ? userID : customerId; }
    @Exclude
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
        if (this.userID == null) {
            this.userID = customerId;
        }
    }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getFurnitureName() { return furnitureName; }
    public void setFurnitureName(String furnitureName) { this.furnitureName = furnitureName; }
    public String getFurnitureImageUrl() { return furnitureImageUrl; }
    public void setFurnitureImageUrl(String furnitureImageUrl) { this.furnitureImageUrl = furnitureImageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getOrderAmount() { return orderAmount; }
    public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
    public boolean isOrderAssemblyReq() { return orderAssemblyReq; }
    public void setOrderAssemblyReq(boolean orderAssemblyReq) { this.orderAssemblyReq = orderAssemblyReq; }
    public String getAssemblerName() { return assemblerName; }
    public void setAssemblerName(String assemblerName) { this.assemblerName = assemblerName; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }
    public Timestamp getCompletionDate() { return completionDate; }
    public void setCompletionDate(Timestamp completionDate) { this.completionDate = completionDate; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Exclude
    public double getTotalPrice() { return orderAmount; }
    @Exclude
    public void setTotalPrice(double totalPrice) { this.orderAmount = totalPrice; }
    @Exclude
    public boolean isAssemblyRequested() { return orderAssemblyReq; }
    @Exclude
    public void setAssemblyRequested(boolean assemblyRequested) { this.orderAssemblyReq = assemblyRequested; }
    @Exclude
    public String getStatus() { return orderStatus; }
    @Exclude
    public void setStatus(String status) { this.orderStatus = status; }
}
