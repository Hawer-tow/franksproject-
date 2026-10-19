package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.Commission;

import java.util.List;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, String> {
    // Optional custom finder
    List<Commission> findByCdate(String cdate);
}
