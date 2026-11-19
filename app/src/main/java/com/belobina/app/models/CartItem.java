package com.belobina.app.models;

import com.google.firebase.Timestamp;

public class CartItem {
    private String cartItemId;
    private String furnitureId;
    private String furnitureName;
    private String furnitureImageUrl;
    private double pricePerUnit;
    private double assemblyFeePerUnit;
    private int quantity;
    private boolean assemblyRequested;
    private Timestamp addedAt;

    public CartItem() {}

    public String getCartItemId() { return cartItemId; }
    public void setCartItemId(String cartItemId) { this.cartItemId = cartItemId; }
    public String getFurnitureId() { return furnitureId; }
    public void setFurnitureId(String furnitureId) { this.furnitureId = furnitureId; }
    public String getFurnitureName() { return furnitureName; }
    public void setFurnitureName(String furnitureName) { this.furnitureName = furnitureName; }
    public String getFurnitureImageUrl() { return furnitureImageUrl; }
    public void setFurnitureImageUrl(String furnitureImageUrl) { this.furnitureImageUrl = furnitureImageUrl; }
    public double getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(double pricePerUnit) { this.pricePerUnit = pricePerUnit; }
    public double getAssemblyFeePerUnit() { return assemblyFeePerUnit; }
    public void setAssemblyFeePerUnit(double assemblyFeePerUnit) { this.assemblyFeePerUnit = assemblyFeePerUnit; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isAssemblyRequested() { return assemblyRequested; }
    public void setAssemblyRequested(boolean assemblyRequested) { this.assemblyRequested = assemblyRequested; }
    public Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }
}
