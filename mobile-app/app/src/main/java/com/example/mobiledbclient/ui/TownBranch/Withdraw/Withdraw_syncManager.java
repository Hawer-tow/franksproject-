package com.example.mobiledbclient.ui.TownBranch.Withdraw;

import android.util.Log;

import com.example.mobiledbclient.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Withdraw_syncManager {
    private final Withdraw_queries dao;
    private final ApiService api;
    private final WithdrawAdapter adapter;

    public Withdraw_syncManager(Withdraw_queries dao, ApiService api, WithdrawAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        pushLocalChanges();
        pullServerChanges();
    }

    private void pushLocalChanges() {
        List<WithdrawEntity> unsynced = dao.getUnsynced();
        for (WithdrawEntity entity : unsynced) {
            if (entity.isMarkedForDelete()) {
                api.deleteWithdraw(entity.getTid()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            dao.deleteByTid(entity.getTid());
                            adapter.updateData(dao.getAll());
                        } else {
                            Log.w("WithdrawSync", "Delete not successful: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("WithdrawSync", "Delete failed", t);
                    }
                });
            } else {
                api.addOrUpdateWithdraw(toDto(entity)).enqueue(new Callback<Withdraw>() {
                    @Override
                    public void onResponse(Call<Withdraw> call, Response<Withdraw> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            entity.setSynced(true);
                            dao.update(entity);
                            adapter.updateWithdraw(entity);
                        } else {
                            Log.w("WithdrawSync", "Push not successful: " + (response == null ? "null response" : response.code()));
                        }
                    }
                    @Override
                    public void onFailure(Call<Withdraw> call, Throwable t) {
                        Log.e("WithdrawSync", "Push failed", t);
                    }
                });
            }
        }
    }

    private void pullServerChanges() {
        api.getWithdraw().enqueue(new Callback<List<Withdraw>>() {
            @Override
            public void onResponse(Call<List<Withdraw>> call, Response<List<Withdraw>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Withdraw> list = response.body();
                    for (Withdraw dto : list) {
                        if (dto.getTid() == null) continue;
                        WithdrawEntity existing = dao.findByTid(dto.getTid());
                        if (existing == null) {
                            WithdrawEntity entity = fromDto(dto);
                            entity.setSynced(true);
                            dao.insert(entity);
                        } else {
                            // update fields
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
                    Log.w("WithdrawSync", "Pull not successful: " + (response == null ? "null" : response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Withdraw>> call, Throwable t) {
                Log.e("WithdrawSync", "Pull failed", t);
            }
        });
    }

    public Withdraw toDto(WithdrawEntity e) {
        Withdraw dto = new Withdraw();
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

    public WithdrawEntity fromDto(Withdraw dto) {
        WithdrawEntity e = new WithdrawEntity();
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
