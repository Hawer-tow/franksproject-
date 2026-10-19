package com.example.mobiledbclient.ui.TownBranch.Withdraw;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "withdraw")
public class WithdrawEntity {
    @PrimaryKey
    @NonNull
    private String tid;

    private String tdate;
    private Double magent, mpersonal, aagent, apersonal;
    private Double EU, EUrate, EA, EArate, JA, JArate;
    private Double MU, MUrate, MA, MArate;
    private Double AO, AOrate, AA, AArate;
    private Double TZ, TZrate, Rwanda, RR;

    private boolean synced;
    private boolean markedForDelete;

    // Getters and setters for all fields
    @NonNull public String getTid() { return tid; }
    public void setTid(@NonNull String tid) { this.tid = tid; }

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
    public void setRwanda(Double rwanda) { Rwanda = rwanda; }

    public Double getRR() { return RR; }
    public void setRR(Double RR) { this.RR = RR; }

    public boolean isSynced() { return synced; }
    public void setSynced(boolean synced) { this.synced = synced; }

    public boolean isMarkedForDelete() { return markedForDelete; }
    public void setMarkedForDelete(boolean markedForDelete) { this.markedForDelete = markedForDelete; }
}
