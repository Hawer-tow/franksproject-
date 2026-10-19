package com.example.mobiledbclient.ui.Kisenyi.openandclosings;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "open_and_close")
public class OpenAndCloseEntity {
    @PrimaryKey
    @NonNull
    private String date;

    private Double ugandaOpening;
    private Double kenyaOpening;
    private Double ugandaCashIn;
    private Double kenyaCashIn;
    private Double ugandaClosing;
    private Double kenyaClosing;
    private String description;

    private boolean synced;
    private boolean markedForDelete;
    // transient UI state, not persisted
    private transient boolean editing = false;



    // Getters and setters
    @NonNull
    public String getDate() { return date; }
    public void setDate(@NonNull String date) { this.date = date; }

    public Double getUgandaOpening() { return ugandaOpening; }
    public void setUgandaOpening(Double ugandaOpening) { this.ugandaOpening = ugandaOpening; }

    public Double getKenyaOpening() { return kenyaOpening; }
    public void setKenyaOpening(Double kenyaOpening) { this.kenyaOpening = kenyaOpening; }

    public Double getUgandaCashIn() { return ugandaCashIn; }
    public void setUgandaCashIn(Double ugandaCashIn) { this.ugandaCashIn = ugandaCashIn; }

    public Double getKenyaCashIn() { return kenyaCashIn; }
    public void setKenyaCashIn(Double kenyaCashIn) { this.kenyaCashIn = kenyaCashIn; }

    public Double getUgandaClosing() { return ugandaClosing; }
    public void setUgandaClosing(Double ugandaClosing) { this.ugandaClosing = ugandaClosing; }

    public Double getKenyaClosing() { return kenyaClosing; }
    public void setKenyaClosing(Double kenyaClosing) { this.kenyaClosing = kenyaClosing; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public boolean isMarkedForDelete() { return markedForDelete; }
    public void setMarkedForDelete(boolean markedForDelete) { this.markedForDelete = markedForDelete; }

    public boolean isEditing() { return editing; }
    public void setEditing(boolean editing) { this.editing = editing; }
}
