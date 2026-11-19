package com.belobina.app.models;

public class Furniture {
    private String id;
    private String prodName;
    private String prodDesc;
    private double prodPrice;
    private double prodAssembleFee;
    private String prodPicture;
    private String prodCat;
    private int prodStock;

    public Furniture() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProdName() { return prodName; }
    public void setProdName(String prodName) { this.prodName = prodName; }
    public String getProdDesc() { return prodDesc; }
    public void setProdDesc(String prodDesc) { this.prodDesc = prodDesc; }
    public double getProdPrice() { return prodPrice; }
    public void setProdPrice(double prodPrice) { this.prodPrice = prodPrice; }
    public double getProdAssembleFee() { return prodAssembleFee; }
    public void setProdAssembleFee(double prodAssembleFee) { this.prodAssembleFee = prodAssembleFee; }
    public String getProdPicture() { return prodPicture; }
    public void setProdPicture(String prodPicture) { this.prodPicture = prodPicture; }
    public String getProdCat() { return prodCat; }
    public void setProdCat(String prodCat) { this.prodCat = prodCat; }
    public int getProdStock() { return prodStock; }
    public void setProdStock(int prodStock) { this.prodStock = prodStock; }
}
