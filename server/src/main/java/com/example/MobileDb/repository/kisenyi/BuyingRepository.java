package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.Buying;

import java.util.List;

@Repository
public interface BuyingRepository extends JpaRepository<Buying, Integer> {

    // Using positional parameter ?1
    @Query(value = "SELECT * FROM buying WHERE Buying_date >= CURRENT_DATE - INTERVAL ?1 DAY", nativeQuery = true)
    List<Buying> findBuyingInRange(int days);

    @Query(value = "SELECT * FROM buying WHERE MONTH(Buying_date) = MONTH(CURRENT_DATE) AND YEAR(Buying_date) = YEAR(CURRENT_DATE)", nativeQuery = true)
    List<Buying> findBuyingMonthly();
    
    Buying findByCheckoutRequestId(String checkoutRequestId);

}
