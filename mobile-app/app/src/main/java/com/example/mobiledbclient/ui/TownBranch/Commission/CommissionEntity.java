package com.example.mobiledbclient.ui.TownBranch.Commission;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "commission")
public class CommissionEntity {
    @PrimaryKey
    @NonNull
    private String cid;

    private String cdate;
    private Double ci;
    private Double co;

    private boolean synced;
    private boolean markedForDelete;

    @NonNull
    public String getCid() { return cid; }
    public void setCid(@NonNull String cid) { this.cid = cid; }

    public String getCdate() { return cdate; }
    public void setCdate(String cdate) { this.cdate = cdate; }

    public Double getCi() { return ci; }
    public void setCi(Double ci) { this.ci = ci; }

    public Double getCo() { return co; }
    public void setCo(Double co) { this.co = co; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public boolean isMarkedForDelete() { return markedForDelete; }
    public void setMarkedForDelete(boolean markedForDelete) { this.markedForDelete = markedForDelete; }
}
