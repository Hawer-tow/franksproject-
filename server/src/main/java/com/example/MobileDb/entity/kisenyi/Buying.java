package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "buying")
public class Buying {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Buying_ID")
    private Integer buyingId;

    @Column(name = "Buying_date", nullable = false)
    private LocalDate buyingDate;


    @Column(name = "Buying_amount", precision = 12, scale = 2)
    private BigDecimal buyingAmount;

    @Column(name = "Buying_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal buyingRate;

    @Column(name = "Transacting_Country", length = 50)
    private String transactingCountry;

    @Column(name = "Buyer_name", length = 70)
    private String buyerName;

    @Column(name = "Buy_Description", length = 255)
    private String buyDescription;
    
    @Column(name = "Checkout_Request_ID", length = 100)
    private String checkoutRequestId;

    public String getCheckoutRequestId() { return checkoutRequestId; }
    public void setCheckoutRequestId(String checkoutRequestId) { this.checkoutRequestId = checkoutRequestId; }


    // Getters and setters
    public Integer getBuyingId() { return buyingId; }
    public void setBuyingId(Integer buyingId) { this.buyingId = buyingId; }
    
    public LocalDate getBuyingDate() { return buyingDate; }
    public void setBuyingDate(LocalDate buyingDate) { this.buyingDate = buyingDate; }


    public BigDecimal getBuyingAmount() { return buyingAmount; }
    public void setBuyingAmount(BigDecimal buyingAmount) { this.buyingAmount = buyingAmount; }

    public BigDecimal getBuyingRate() { return buyingRate; }
    public void setBuyingRate(BigDecimal buyingRate) { this.buyingRate = buyingRate; }

    public String getTransactingCountry() { return transactingCountry; }
    public void setTransactingCountry(String transactingCountry) { this.transactingCountry = transactingCountry; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyDescription() { return buyDescription; }
    public void setBuyDescription(String buyDescription) { this.buyDescription = buyDescription; }
}
