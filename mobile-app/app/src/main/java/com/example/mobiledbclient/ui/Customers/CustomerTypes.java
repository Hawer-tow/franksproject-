package com.example.mobiledbclient.ui.Customers;

public class CustomerTypes {
    private int id;
    private int iconRes;
    private String description;

    public CustomerTypes(int id, int iconRes, String description) {
        this.id = id;
        this.iconRes = iconRes;
        this.description = description;
    }

    public int getId() { return id; }
    public int getIconRes() { return iconRes; }
    public String getDescription() { return description; }
}
