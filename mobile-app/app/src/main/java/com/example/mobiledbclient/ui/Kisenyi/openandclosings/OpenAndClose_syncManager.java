package com.example.mobiledbclient.ui.Kisenyi.openandclosings;

import com.example.mobiledbclient.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenAndClose_syncManager {
    private final OpenAndClose_queries dao;
    private final ApiService api;
    private final OpenAndCloseAdapter adapter;

    // Background sync (no adapter)
    public OpenAndClose_syncManager(OpenAndClose_queries dao, ApiService api) {
        this.dao = dao;
        this.api = api;
        this.adapter = null;
    }

    // UI sync (with adapter)
    public OpenAndClose_syncManager(OpenAndClose_queries dao, ApiService api, OpenAndCloseAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        // Push unsynced local records to server
        List<OpenAndCloseEntity> unsynced = dao.getUnsynced();
        for (OpenAndCloseEntity entity : unsynced) {
            entity.setDate(normalizeDate(entity.getDate()));

            if (entity.isMarkedForDelete()) {
                api.deleteOpenAndClose(entity.getDate()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            dao.deleteByDate(entity.getDate());
                            if (adapter != null) {
                                adapter.updateData(dao.getAll());
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        android.util.Log.e("OpenAndCloseSync", "Delete failed", t);
                    }
                });
            } else {
                api.addOrUpdateOpenAndClose(toDto(entity)).enqueue(new Callback<OpenAndClose>() {
                    @Override
                    public void onResponse(Call<OpenAndClose> call, Response<OpenAndClose> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            entity.setDate(normalizeDate(response.body().getDate()));
                            entity.setSynced(true);
                            dao.update(entity);
                            if (adapter != null) {
                                adapter.updateData(dao.getAll());
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<OpenAndClose> call, Throwable t) {
                        android.util.Log.e("OpenAndCloseSync", "Sync failed", t);
                    }
                });
            }
        }

        // Pull latest from server
        api.getOpenAndClose().enqueue(new Callback<List<OpenAndClose>>() {
            @Override
            public void onResponse(Call<List<OpenAndClose>> call, Response<List<OpenAndClose>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (OpenAndClose dto : response.body()) {
                        String normalizedDate = normalizeDate(dto.getDate());
                        OpenAndCloseEntity existing = dao.findByDate(normalizedDate);
                        if (existing == null) {
                            dao.insert(fromDto(dto));
                        } else {
                            existing.setUgandaOpening(dto.getUgandaOpening());
                            existing.setKenyaOpening(dto.getKenyaOpening());
                            existing.setUgandaCashIn(dto.getUgandaCashIn());
                            existing.setKenyaCashIn(dto.getKenyaCashIn());
                            existing.setUgandaClosing(dto.getUgandaClosing());
                            existing.setKenyaClosing(dto.getKenyaClosing());
                            existing.setDescription(dto.getDescription());
                            existing.setDate(normalizedDate);
                            existing.setSynced(true);
                            dao.update(existing);
                        }
                    }
                    if (adapter != null) {
                        adapter.updateData(dao.getAll());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<OpenAndClose>> call, Throwable t) {
                android.util.Log.e("OpenAndCloseSync", "Fetch failed", t);
            }
        });
    }

    public OpenAndClose toDto(OpenAndCloseEntity entity) {
        OpenAndClose dto = new OpenAndClose();
        dto.setDate(normalizeDate(entity.getDate()));
        dto.setUgandaOpening(entity.getUgandaOpening());
        dto.setKenyaOpening(entity.getKenyaOpening());
        dto.setUgandaCashIn(entity.getUgandaCashIn());
        dto.setKenyaCashIn(entity.getKenyaCashIn());
        dto.setUgandaClosing(entity.getUgandaClosing());
        dto.setKenyaClosing(entity.getKenyaClosing());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    public OpenAndCloseEntity fromDto(OpenAndClose dto) {
        OpenAndCloseEntity entity = new OpenAndCloseEntity();
        entity.setDate(normalizeDate(dto.getDate()));
        entity.setUgandaOpening(dto.getUgandaOpening());
        entity.setKenyaOpening(dto.getKenyaOpening());
        entity.setUgandaCashIn(dto.getUgandaCashIn());
        entity.setKenyaCashIn(dto.getKenyaCashIn());
        entity.setUgandaClosing(dto.getUgandaClosing());
        entity.setKenyaClosing(dto.getKenyaClosing());
        entity.setDescription(dto.getDescription());
        entity.setSynced(true);
        return entity;
    }

    private String normalizeDate(String rawDate) {
        if (rawDate == null) return null;
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }
}
