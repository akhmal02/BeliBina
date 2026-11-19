package com.belobina.app.models;

public class Assembler {
    private String id;
    private String assFullName;
    private String assEmail;
    private String assPhone;
    private String assServiceArea;

    public Assembler() {}

    public Assembler(String assFullName, String assEmail, String assPhone, String assServiceArea) {
        this.assFullName = assFullName;
        this.assEmail = assEmail;
        this.assPhone = assPhone;
        this.assServiceArea = assServiceArea;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAssFullName() { return assFullName; }
    public void setAssFullName(String assFullName) { this.assFullName = assFullName; }
    public String getAssEmail() { return assEmail; }
    public void setAssEmail(String assEmail) { this.assEmail = assEmail; }
    public String getAssPhone() { return assPhone; }
    public void setAssPhone(String assPhone) { this.assPhone = assPhone; }
    public String getAssServiceArea() { return assServiceArea; }
    public void setAssServiceArea(String assServiceArea) { this.assServiceArea = assServiceArea; }
}
