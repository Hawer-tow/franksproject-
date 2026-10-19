package com.example.mobiledbclient.ui.TownBranch.Commission;

import android.util.Log;

import com.example.mobiledbclient.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Commission_syncManager {
    private final Commission_queries dao;
    private final ApiService api;
    private final CommissionAdapter adapter;

    public Commission_syncManager(Commission_queries dao, ApiService api, CommissionAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        pushLocalChanges();
        pullServerChanges();
    }

    private void pushLocalChanges() {
        List<CommissionEntity> unsynced = dao.getUnsynced();
        for (CommissionEntity entity : unsynced) {
            if (entity.isMarkedForDelete()) {
                api.deleteCommission(entity.getCid()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            dao.deleteByCid(entity.getCid());
                            adapter.updateData(dao.getAll());
                        } else {
                            Log.w("CommissionSync", "Delete not successful: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("CommissionSync", "Delete failed", t);
                    }
                });
            } else {
                api.addOrUpdateCommission(toDto(entity)).enqueue(new Callback<Commission>() {
                    @Override
                    public void onResponse(Call<Commission> call, Response<Commission> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            entity.setSynced(true);
                            dao.update(entity);
                            adapter.updateCommission(entity);
                        } else {
                            Log.w("CommissionSync", "Push not successful: " + (response == null ? "null response" : response.code()));
                        }
                    }
                    @Override
                    public void onFailure(Call<Commission> call, Throwable t) {
                        Log.e("CommissionSync", "Push failed", t);
                    }
                });
            }
        }
    }

    private void pullServerChanges() {
        api.getCommission().enqueue(new Callback<List<Commission>>() {
            @Override
            public void onResponse(Call<List<Commission>> call, Response<List<Commission>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Commission> list = response.body();
                    for (Commission dto : list) {
                        if (dto.getCid() == null) continue;
                        CommissionEntity existing = dao.findByCid(dto.getCid());
                        if (existing == null) {
                            CommissionEntity entity = fromDto(dto);
                            entity.setSynced(true);
                            dao.insert(entity);
                        } else {
                            existing.setCdate(dto.getCdate());
                            existing.setCi(dto.getCi());
                            existing.setCo(dto.getCo());
                            existing.setSynced(true);
                            dao.update(existing);
                        }
                    }
                    adapter.updateData(dao.getAll());
                } else {
                    Log.w("CommissionSync", "Pull not successful: " + (response == null ? "null" : response.code()));
                }
            }

            @Override
            public void onFailure(Call<List<Commission>> call, Throwable t) {
                Log.e("CommissionSync", "Pull failed", t);
            }
        });
    }

    public Commission toDto(CommissionEntity e) {
        Commission dto = new Commission();
        dto.setCid(e.getCid());
        dto.setCdate(e.getCdate());
        dto.setCi(e.getCi());
        dto.setCo(e.getCo());
        return dto;
    }

    public CommissionEntity fromDto(Commission dto) {
        CommissionEntity e = new CommissionEntity();
        e.setCid(dto.getCid());
        e.setCdate(dto.getCdate());
        e.setCi(dto.getCi());
        e.setCo(dto.getCo());
        e.setSynced(true);
        return e;
    }
}
