package com.example.mobiledbclient.ui.MajorTransactions;




public class TransactionsItems {
    private int id;
    private int iconRes;
    private String description;
    private boolean isHeader;

    // Constructor for normal items
    public TransactionsItems(int id, int iconRes, String description) {
        this.id = id;
        this.iconRes = iconRes;
        this.description = description;
        this.isHeader = false;
    }

    // Constructor for headers
    public TransactionsItems(String description) {
        this.id = -1;
        this.iconRes = 0;
        this.description = description;
        this.isHeader = true;
    }

    public int getId() { return id; }
    public int getIconRes() { return iconRes; }
    public String getDescription() { return description; }
    public boolean isHeader() { return isHeader; }
}
