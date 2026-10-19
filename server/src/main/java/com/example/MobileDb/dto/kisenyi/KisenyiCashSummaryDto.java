package com.example.MobileDb.dto.kisenyi;

public class KisenyiCashSummaryDto {
    private int sellingTotal;          // b
    private int ugandaOpeningCash;     // d
    private int buyingTotal;           // a
    private int expensesTotal;         // c
    private int netCash;               // e = (b+d) - (a+c)
    private int finalClosingCash;      // f

    // Getters and setters
    public int getSellingTotal() { return sellingTotal; }
    public void setSellingTotal(int sellingTotal) { this.sellingTotal = sellingTotal; }

    public int getUgandaOpeningCash() { return ugandaOpeningCash; }
    public void setUgandaOpeningCash(int ugandaOpeningCash) { this.ugandaOpeningCash = ugandaOpeningCash; }

    public int getBuyingTotal() { return buyingTotal; }
    public void setBuyingTotal(int buyingTotal) { this.buyingTotal = buyingTotal; }

    public int getExpensesTotal() { return expensesTotal; }
    public void setExpensesTotal(int expensesTotal) { this.expensesTotal = expensesTotal; }

    public int getNetCash() { return netCash; }
    public void setNetCash(int netCash) { this.netCash = netCash; }

    public int getFinalClosingCash() { return finalClosingCash; }
    public void setFinalClosingCash(int finalClosingCash) { this.finalClosingCash = finalClosingCash; }
}
