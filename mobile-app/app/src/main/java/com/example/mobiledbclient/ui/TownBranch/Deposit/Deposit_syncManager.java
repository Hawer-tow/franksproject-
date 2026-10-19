package com.example.mobiledbclient.ui.TownBranch.Deposit;

import android.util.Log;

import com.example.mobiledbclient.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Deposit_syncManager {
    private final Deposit_queries dao;
    private final ApiService api;
    private final DepositAdapter adapter;

    public Deposit_syncManager(Deposit_queries dao, ApiService api, DepositAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        pushLocalChanges();
        pullServerChanges();
    }

    private void pushLocalChanges() {
        List<DepositEntity> unsynced = dao.getUnsynced();
        for (DepositEntity entity : unsynced) {
            if (entity.isMarkedForDelete()) {
                api.deleteDeposit(entity.getTid()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            dao.deleteByTid(entity.getTid());
                            adapter.updateData(dao.getAll());
                        } else {
                            Log.w("DepositSync", "Delete not successful: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("DepositSync", "Delete failed", t);
                    }
                });
            } else {
                api.addOrUpdateDeposit(toDto(entity)).enqueue(new Callback<Deposit>() {
                    @Override
                    public void onResponse(Call<Deposit> call, Response<Deposit> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            entity.setSynced(true);
                            dao.update(entity);
                            adapter.updateDeposit(entity);
                        } else {
                            Log.w("DepositSync", "Push not successful: " + (response == null ? "null response" : response.code()));
                        }
                    }
                    @Override
                    public void onFailure(Call<Deposit> call, Throwable t) {
                        Log.e("DepositSync", "Push failed", t);
                    }
                });
            }
        }
    }

    private void pullServerChanges() {
        api.getDeposit().enqueue(new Callback<List<Deposit>>() {
            @Override
            public void onResponse(Call<List<Deposit>> call, Response<List<Deposit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Deposit> list = response.body();
                    for (Deposit dto : list) {
                        if (dto.getTid() == null) continue;
                        DepositEntity existing = dao.findByTid(dto.getTid());
                        if (existing == null) {
                            DepositEntity entity = fromDto(dto);
                            entity.setSynced(true);
                            dao.insert(entity);
                        } else {
                            existing.setTdate(dto.getTdate());
                            existing.setMagent(dto.getMagent());
                            existing.setMpersonal(dto.getMpersonal());
                            existing.setAagent(dto.getAagent());
                            existing.setApersonal(dto.getApersonal());
                            existing.setEU(dto.getEU());
                            existing.setEUrate(dto.getEUrate());
                            existing.setEA(dto.getEA());
                            existing.setEArate(dto.getEArate());
                            existing.setJA(dto.getJA());
                            existing.setJArate(dto.getJArate());
                            existing.setMU(dto.getMU());
                            existing.setMUrate(dto.getMUrate());
                            existing.setMA(dto.getMA());
                            existing.setMArate(dto.getMArate());
                            existing.setAO(dto.getAO());
                            existing.setAOrate(dto.getAOrate());
                            existing.setAA(dto.getAA());
                            existing.setAArate(dto.getAArate());
                            existing.setTZ(dto.getTZ());
                            existing.setTZrate(dto.getTZrate());
                            existing.setRwanda(dto.getRwanda());
                            existing.setRR(dto.getRR());
                            existing.setSynced(true);
                            dao.update(existing);
                        }
                    }
                    adapter.updateData(dao.getAll());
                } else {
                    Log.w("DepositSync", "Pull not successful: " + (response == null ? "null" : response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Deposit>> call, Throwable t) {
                Log.e("DepositSync", "Pull failed", t);
            }
        });
    }

    public Deposit toDto(DepositEntity e) {
        Deposit dto = new Deposit();
        dto.setTid(e.getTid());
        dto.setTdate(e.getTdate());
        dto.setMagent(e.getMagent());
        dto.setMpersonal(e.getMpersonal());
        dto.setAagent(e.getAagent());
        dto.setApersonal(e.getApersonal());
        dto.setEU(e.getEU());
        dto.setEUrate(e.getEUrate());
        dto.setEA(e.getEA());
        dto.setEArate(e.getEArate());
        dto.setJA(e.getJA());
        dto.setJArate(e.getJArate());
        dto.setMU(e.getMU());
        dto.setMUrate(e.getMUrate());
        dto.setMA(e.getMA());
        dto.setMArate(e.getMArate());
        dto.setAO(e.getAO());
        dto.setAOrate(e.getAOrate());
        dto.setAA(e.getAA());
        dto.setAArate(e.getAArate());
        dto.setTZ(e.getTZ());
        dto.setTZrate(e.getTZrate());
        dto.setRwanda(e.getRwanda());
        dto.setRR(e.getRR());
        return dto;
    }

    public DepositEntity fromDto(Deposit dto) {
        DepositEntity e = new DepositEntity();
        e.setTid(dto.getTid());
        e.setTdate(dto.getTdate());
        e.setMagent(dto.getMagent());
        e.setMpersonal(dto.getMpersonal());
        e.setAagent(dto.getAagent());
        e.setApersonal(dto.getApersonal());
        e.setEU(dto.getEU());
        e.setEUrate(dto.getEUrate());
        e.setEA(dto.getEA());
        e.setEArate(dto.getEArate());
        e.setJA(dto.getJA());
        e.setJArate(dto.getJArate());
        e.setMU(dto.getMU());
        e.setMUrate(dto.getMUrate());
        e.setMA(dto.getMA());
        e.setMArate(dto.getMArate());
        e.setAO(dto.getAO());
        e.setAOrate(dto.getAOrate());
        e.setAA(dto.getAA());
        e.setAArate(dto.getAArate());
        e.setTZ(dto.getTZ());
        e.setTZrate(dto.getTZrate());
        e.setRwanda(dto.getRwanda());
        e.setRR(dto.getRR());
        e.setSynced(true);
        return e;
    }
}
