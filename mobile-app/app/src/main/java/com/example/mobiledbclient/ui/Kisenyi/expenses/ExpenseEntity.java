package com.example.mobiledbclient.ui.Kisenyi.expenses;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private int localId;

    private Integer serverId;
    private double amount;
    private String date;
    private String description;

    private boolean synced;
    private boolean markedForDelete;

    @Ignore
    private boolean editing; // UI-only

    // Getters/setters
    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public Integer getServerId() { return serverId; }
    public void setServerId(Integer serverId) { this.serverId = serverId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public boolean isMarkedForDelete() { return markedForDelete; }
    public void setMarkedForDelete(boolean markedForDelete) { this.markedForDelete = markedForDelete; }

    public boolean isEditing() { return editing; }
    public void setEditing(boolean editing) { this.editing = editing; }
}
