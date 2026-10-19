package com.example.mobiledbclient.ui.Kisenyi.buys;

import com.example.mobiledbclient.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class Buying_syncManager {
    private final Buying_queries dao;
    private final ApiService api;
    private final BuyingAdapter adapter;

    // Background sync (no adapter)
    public Buying_syncManager(Buying_queries dao, ApiService api) {
        this.dao = dao;
        this.api = api;
        this.adapter = null;
    }

    // UI sync (with adapter)
    public Buying_syncManager(Buying_queries dao, ApiService api, BuyingAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        List<BuyingEntity> unsynced = dao.getUnsynced();
        for (BuyingEntity b : unsynced) {
            b.setBuyingDate(normalizeDate(b.getBuyingDate()));
            Integer serverId = b.getServerId();

            if (b.isMarkedForDelete()) {
                if (serverId != null) {
                    api.deleteBuying(serverId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            dao.deleteById(b.getLocalId());
                            if (adapter != null) {
                                adapter.removeBuying(b);
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            // log or retry
                        }
                    });
                } else {
                    dao.deleteById(b.getLocalId());
                    if (adapter != null) {
                        adapter.removeBuying(b);
                    }
                }
            } else if (serverId == null) {
                api.addBuying(toDto(b)).enqueue(new Callback<Buying>() {
                    @Override
                    public void onResponse(Call<Buying> call, Response<Buying> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            b.setServerId(response.body().getBuyingId());
                            b.setBuyingDate(normalizeDate(response.body().getBuyingDate()));
                            b.setSynced(true);
                            dao.update(b);
                            if (adapter != null) {
                                adapter.updateBuying(b);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Buying> call, Throwable t) {
                        // log or retry
                    }
                });
            } else {
                api.updateBuying(serverId, toDto(b)).enqueue(new Callback<Buying>() {
                    @Override
                    public void onResponse(Call<Buying> call, Response<Buying> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            b.setBuyingDate(normalizeDate(response.body().getBuyingDate()));
                            b.setSynced(true);
                            dao.update(b);
                            if (adapter != null) {
                                adapter.updateBuying(b);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Buying> call, Throwable t) {
                        // log or retry
                    }
                });
            }
        }
    }

    private Buying toDto(BuyingEntity entity) {
        Buying dto = new Buying();
        dto.setBuyingId(entity.getServerId());
        dto.setBuyingDate(normalizeDate(entity.getBuyingDate()));
        dto.setBuyingAmount(entity.getBuyingAmount());
        dto.setBuyingRate(entity.getBuyingRate());
        dto.setTransactingCountry(entity.getTransactingCountry());
        dto.setBuyerName(entity.getBuyerName());
        dto.setBuyDescription(entity.getBuyDescription());
        return dto;
    }

    private String normalizeDate(String rawDate) {
        if (rawDate == null) return null;
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }
}
