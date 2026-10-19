package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "open_and_close")
public class OpenAndClose {
	@Id
	@Column(name = "date", nullable = false)
	private LocalDate date;


    @Column(name = "uganda_opening", precision = 12, scale = 2)
    private BigDecimal ugandaOpening;

    @Column(name = "kenya_opening", precision = 12, scale = 2)
    private BigDecimal kenyaOpening;

    @Column(name = "uganda_cash_in", precision = 12, scale = 2)
    private BigDecimal ugandaCashIn;

    @Column(name = "kenya_cash_in", precision = 12, scale = 2)
    private BigDecimal kenyaCashIn;

    @Column(name = "uganda_closing", precision = 12, scale = 2)
    private BigDecimal ugandaClosing;

    @Column(name = "kenya_closing", precision = 12, scale = 2)
    private BigDecimal kenyaClosing;

    @Column(name = "description", length = 40)
    private String description;

    // Getters and setters

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    
    public BigDecimal getUgandaOpening() { return ugandaOpening; }
    public void setUgandaOpening(BigDecimal ugandaOpening) { this.ugandaOpening = ugandaOpening; }

    public BigDecimal getKenyaOpening() { return kenyaOpening; }
    public void setKenyaOpening(BigDecimal kenyaOpening) { this.kenyaOpening = kenyaOpening; }

    public BigDecimal getUgandaCashIn() { return ugandaCashIn; }
    public void setUgandaCashIn(BigDecimal ugandaCashIn) { this.ugandaCashIn = ugandaCashIn; }

    public BigDecimal getKenyaCashIn() { return kenyaCashIn; }
    public void setKenyaCashIn(BigDecimal kenyaCashIn) { this.kenyaCashIn = kenyaCashIn; }

    public BigDecimal getUgandaClosing() { return ugandaClosing; }
    public void setUgandaClosing(BigDecimal ugandaClosing) { this.ugandaClosing = ugandaClosing; }

    public BigDecimal getKenyaClosing() { return kenyaClosing; }
    public void setKenyaClosing(BigDecimal kenyaClosing) { this.kenyaClosing = kenyaClosing; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
