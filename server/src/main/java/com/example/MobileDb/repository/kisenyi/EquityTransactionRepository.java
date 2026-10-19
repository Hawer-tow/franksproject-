package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.EquityTransaction;

public interface EquityTransactionRepository extends JpaRepository<EquityTransaction, Long> {
    EquityTransaction findByTransactionId(String transactionId);
}

