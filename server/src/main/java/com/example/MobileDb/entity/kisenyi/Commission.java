package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "commission")
public class Commission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "Cid", nullable = false, length = 64)
    private String cid;

    @Column(name = "CD")
    private String cdate;

    @Column(name = "CI")
    private Double ci;

    @Column(name = "CO")
    private Double co;

    public Commission() {}

    public String getCid() { return cid; }
    public void setCid(String cid) { this.cid = cid; }

    public String getCdate() { return cdate; }
    public void setCdate(String cdate) { this.cdate = cdate; }

    public Double getCi() { return ci; }
    public void setCi(Double ci) { this.ci = ci; }

    public Double getCo() { return co; }
    public void setCo(Double co) { this.co = co; }
}
