package com.example.mobiledbclient.ui.Kisenyi.sales;

import com.example.mobiledbclient.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class Selling_syncManager {
    private final Selling_queries dao;
    private final ApiService api;
    private final SellingAdapter adapter;

    // Background sync (no adapter)
    public Selling_syncManager(Selling_queries dao, ApiService api) {
        this.dao = dao;
        this.api = api;
        this.adapter = null;
    }

    // UI sync (with adapter)
    public Selling_syncManager(Selling_queries dao, ApiService api, SellingAdapter adapter) {
        this.dao = dao;
        this.api = api;
        this.adapter = adapter;
    }

    public void sync() {
        List<SellingEntity> unsynced = dao.getUnsynced();
        for (SellingEntity s : unsynced) {
            // Normalize date before syncing
            s.setSellingDate(normalizeDate(s.getSellingDate()));

            // Normalize country (Kenya if +254)
            s.setReceivingCountry(normalizeCountry(s.getPhoneNo(), s.getReceivingCountry()));

            if (s.isMarkedForDelete()) {
                api.deleteSelling(s.getServerId()).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        dao.deleteById(s.getLocalId());
                        if (adapter != null) {
                            adapter.removeSelling(s);
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            } else if (s.getServerId() == null) {
                api.addSelling(toDto(s)).enqueue(new Callback<Selling>() {
                    @Override
                    public void onResponse(Call<Selling> call, Response<Selling> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Selling dto = response.body();
                            s.setServerId(dto.getSellingId());
                            s.setSellingDate(normalizeDate(dto.getSellingDate()));
                            s.setSynced(true);

                            // --- set saleType based on backend fields ---
                            s.setSaleType(determineSaleType(dto));

                            // For MPESA system transactions, feed description from status
                            if ("MPESA".equalsIgnoreCase(s.getSaleType()) && dto.getStatus() != null) {
                                s.setSellDescription(dto.getStatus());
                            }

                            // Normalize country again
                            s.setReceivingCountry(normalizeCountry(dto.getPhoneNo(), dto.getReceivingCountry()));

                            dao.update(s);
                            if (adapter != null) {
                                adapter.updateSelling(s);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Selling> call, Throwable t) {}
                });
            } else {
                api.updateSelling(s.getServerId(), toDto(s)).enqueue(new Callback<Selling>() {
                    @Override
                    public void onResponse(Call<Selling> call, Response<Selling> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Selling dto = response.body();
                            s.setSellingDate(normalizeDate(dto.getSellingDate()));
                            s.setSynced(true);

                            // --- set saleType based on backend fields ---
                            s.setSaleType(determineSaleType(dto));

                            // For MPESA system transactions, feed description from status
                            if ("MPESA".equalsIgnoreCase(s.getSaleType()) && dto.getStatus() != null) {
                                s.setSellDescription(dto.getStatus());
                            }

                            // Normalize country again
                            s.setReceivingCountry(normalizeCountry(dto.getPhoneNo(), dto.getReceivingCountry()));

                            dao.update(s);
                            if (adapter != null) {
                                adapter.updateSelling(s);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<Selling> call, Throwable t) {}
                });
            }
        }
    }

    private Selling toDto(SellingEntity entity) {
        Selling dto = new Selling();
        dto.setSellingId(entity.getServerId());
        dto.setSellingDate(normalizeDate(entity.getSellingDate()));
        dto.setSellingAmount(entity.getSellingAmount());
        dto.setSellingRate(entity.getSellingRate());
        dto.setReceivingCountry(entity.getReceivingCountry());
        dto.setPhoneNo(entity.getPhoneNo());
        dto.setRecipientName(entity.getRecipientName());
        dto.setSellDescription(entity.getSellDescription());
        dto.setTransactionId(entity.getTransactionId());
        dto.setStatus(entity.getStatus());

        // --- Bank-related fields ---
        dto.setSenderAccount(entity.getSenderAccount());
        dto.setReceiverAccount(entity.getReceiverAccount());
        dto.setBankCode(entity.getBankCode());
        dto.setCurrency(entity.getCurrency());

        // --- saleType ---
        dto.setSaleType(entity.getSaleType());

        return dto;
    }

    private String normalizeDate(String rawDate) {
        if (rawDate == null) return null;
        return rawDate.length() >= 10 ? rawDate.substring(0, 10) : rawDate;
    }

    private String normalizeCountry(String phoneNo, String backendCountry) {
        if (phoneNo != null && phoneNo.startsWith("254")) {
            return "Kenya";
        }
        return backendCountry != null ? backendCountry : "Kenya";
    }

    private String determineSaleType(Selling dto) {
        if (dto.getTransactionId() != null && !dto.getTransactionId().isEmpty()) {
            return "MPESA";
        } else if (dto.getBankCode() != null && !dto.getBankCode().isEmpty()) {
            return "BANK";
        } else {
            return "USER_INITIATED";
        }
    }
}
