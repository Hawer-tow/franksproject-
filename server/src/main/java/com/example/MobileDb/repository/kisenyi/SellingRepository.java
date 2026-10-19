package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.Selling;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellingRepository extends JpaRepository<Selling, Integer> {
	
	 

    // Sales in the last N days (truncate to DATE so time portion doesn't break comparisons)
    @Query(value = "SELECT * FROM selling " +
                   "WHERE DATE(Selling_date) >= CURRENT_DATE - INTERVAL :days DAY",
           nativeQuery = true)
    List<Selling> findSellingInRange(@Param("days") int days);

    // Sales in the current month (truncate to DATE for consistency)
    @Query(value = "SELECT * FROM selling " +
                   "WHERE MONTH(DATE(Selling_date)) = MONTH(CURRENT_DATE) " +
                   "AND YEAR(DATE(Selling_date)) = YEAR(CURRENT_DATE)",
           nativeQuery = true)
    List<Selling> findSellingMonthly();

    // Lookup by transactionId (OriginatorConversationID initially, later updated to final TransactionID)
    Optional<Selling> findByTransactionId(String transactionId);

    // Efficient lookup by OriginatorConversationID for callback correlation
    Optional<Selling> findByOriginatorConversationId(String originatorConversationId);
}
