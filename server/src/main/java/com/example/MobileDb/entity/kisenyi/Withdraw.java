package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "withdraw")
public class Withdraw implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "Tid", nullable = false, length = 64)
    private String tid;

    @Column(name = "Tdate", nullable = false)
    private String tdate;

    @Column(name = "magent")
    private Double magent;

    @Column(name = "mpersonal")
    private Double mpersonal;

    @Column(name = "aagent")
    private Double aagent;

    @Column(name = "apersonal")
    private Double apersonal;

    @Column(name = "EU")
    private Double EU;

    @Column(name = "EUrate")
    private Double EUrate;

    @Column(name = "EA")
    private Double EA;

    @Column(name = "EArate")
    private Double EArate;

    @Column(name = "JA")
    private Double JA;

    @Column(name = "JArate")
    private Double JArate;

    @Column(name = "MU")
    private Double MU;

    @Column(name = "MUrate")
    private Double MUrate;

    @Column(name = "MA")
    private Double MA;

    @Column(name = "MArate")
    private Double MArate;

    @Column(name = "AO")
    private Double AO;

    @Column(name = "AOrate")
    private Double AOrate;

    @Column(name = "AA")
    private Double AA;

    @Column(name = "AArate")
    private Double AArate;

    @Column(name = "TZ")
    private Double TZ;

    @Column(name = "TZrate")
    private Double TZrate;

    @Column(name = "Rwanda")
    private Double Rwanda;

    @Column(name = "RR")
    private Double RR;

    public Withdraw() {}

    // Getters and setters
    public String getTid() { return tid; }
    public void setTid(String tid) { this.tid = tid; }

    public String getTdate() { return tdate; }
    public void setTdate(String tdate) { this.tdate = tdate; }

    public Double getMagent() { return magent; }
    public void setMagent(Double magent) { this.magent = magent; }

    public Double getMpersonal() { return mpersonal; }
    public void setMpersonal(Double mpersonal) { this.mpersonal = mpersonal; }

    public Double getAagent() { return aagent; }
    public void setAagent(Double aagent) { this.aagent = aagent; }

    public Double getApersonal() { return apersonal; }
    public void setApersonal(Double apersonal) { this.apersonal = apersonal; }

    public Double getEU() { return EU; }
    public void setEU(Double EU) { this.EU = EU; }

    public Double getEUrate() { return EUrate; }
    public void setEUrate(Double EUrate) { this.EUrate = EUrate; }

    public Double getEA() { return EA; }
    public void setEA(Double EA) { this.EA = EA; }

    public Double getEArate() { return EArate; }
    public void setEArate(Double EArate) { this.EArate = EArate; }

    public Double getJA() { return JA; }
    public void setJA(Double JA) { this.JA = JA; }

    public Double getJArate() { return JArate; }
    public void setJArate(Double JArate) { this.JArate = JArate; }

    public Double getMU() { return MU; }
    public void setMU(Double MU) { this.MU = MU; }

    public Double getMUrate() { return MUrate; }
    public void setMUrate(Double MUrate) { this.MUrate = MUrate; }

    public Double getMA() { return MA; }
    public void setMA(Double MA) { this.MA = MA; }

    public Double getMArate() { return MArate; }
    public void setMArate(Double MArate) { this.MArate = MArate; }

    public Double getAO() { return AO; }
    public void setAO(Double AO) { this.AO = AO; }

    public Double getAOrate() { return AOrate; }
    public void setAOrate(Double AOrate) { this.AOrate = AOrate; }

    public Double getAA() { return AA; }
    public void setAA(Double AA) { this.AA = AA; }

    public Double getAArate() { return AArate; }
    public void setAArate(Double AArate) { this.AArate = AArate; }

    public Double getTZ() { return TZ; }
    public void setTZ(Double TZ) { this.TZ = TZ; }

    public Double getTZrate() { return TZrate; }
    public void setTZrate(Double TZrate) { this.TZrate = TZrate; }

    public Double getRwanda() { return Rwanda; }
    public void setRwanda(Double rwanda) { this.Rwanda = rwanda; }

    public Double getRR() { return RR; }
    public void setRR(Double RR) { this.RR = RR; }
}
