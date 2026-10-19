package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import com.example.MobileDb.entity.kisenyi.OpenAndClose;

import java.util.List;

@Repository
public interface OpenAndCloseRepository extends JpaRepository<OpenAndClose, LocalDate> {

    // Records in the last N days
    @Query(value = "SELECT * FROM open_and_close WHERE date >= CURRENT_DATE - INTERVAL :days DAY", nativeQuery = true)
    List<OpenAndClose> findInRange(@Param("days") int days);

    // Records in the current month
    @Query(value = "SELECT * FROM open_and_close WHERE MONTH(date) = MONTH(CURRENT_DATE) AND YEAR(date) = YEAR(CURRENT_DATE)", nativeQuery = true)
    List<OpenAndClose> findMonthly();
}
