package com.example.mobiledbclient.ui.Kisenyi.buys;

public class Buying {
    private Integer buyingId;
    private String buyingDate;       // ISO date string
    private Double buyingAmount;
    private Double buyingRate;
    private String transactingCountry;
    private String buyerName;
    private String buyDescription;

    public Integer getBuyingId() { return buyingId; }
    public void setBuyingId(Integer buyingId) { this.buyingId = buyingId; }

    public String getBuyingDate() { return buyingDate; }
    public void setBuyingDate(String buyingDate) { this.buyingDate = buyingDate; }

    public Double getBuyingAmount() { return buyingAmount; }
    public void setBuyingAmount(Double buyingAmount) { this.buyingAmount = buyingAmount; }

    public Double getBuyingRate() { return buyingRate; }
    public void setBuyingRate(Double buyingRate) { this.buyingRate = buyingRate; }

    public String getTransactingCountry() { return transactingCountry; }
    public void setTransactingCountry(String transactingCountry) { this.transactingCountry = transactingCountry; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyDescription() { return buyDescription; }
    public void setBuyDescription(String buyDescription) { this.buyDescription = buyDescription; }
}
