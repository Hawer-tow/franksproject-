package com.example.mobiledbclient.ui.Kisenyi.buys;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "buying")
public class BuyingEntity {
    @PrimaryKey(autoGenerate = true)
    private int localId;

    private Integer serverId;
    private String buyingDate;
    private double buyingAmount;
    private double buyingRate;
    private String transactingCountry;
    private String buyerName;
    private String buyDescription;

    private boolean synced;
    private boolean markedForDelete;

    @Ignore
    private boolean editing; // UI-only

    // getters and setters...
    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public Integer getServerId() { return serverId; }
    public void setServerId(Integer serverId) { this.serverId = serverId; }

    public String getBuyingDate() { return buyingDate; }
    public void setBuyingDate(String buyingDate) { this.buyingDate = buyingDate; }

    public double getBuyingAmount() { return buyingAmount; }
    public void setBuyingAmount(double buyingAmount) { this.buyingAmount = buyingAmount; }

    public double getBuyingRate() { return buyingRate; }
    public void setBuyingRate(double buyingRate) { this.buyingRate = buyingRate; }

    public String getTransactingCountry() { return transactingCountry; }
    public void setTransactingCountry(String transactingCountry) { this.transactingCountry = transactingCountry; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyDescription() { return buyDescription; }
    public void setBuyDescription(String buyDescription) { this.buyDescription = buyDescription; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public boolean isMarkedForDelete() { return markedForDelete; }
    public void setMarkedForDelete(boolean markedForDelete) { this.markedForDelete = markedForDelete; }

    public boolean isEditing() { return editing; }
    public void setEditing(boolean editing) { this.editing = editing; }
}
