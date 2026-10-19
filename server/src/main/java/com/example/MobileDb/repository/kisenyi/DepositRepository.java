package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.Deposit;

import java.util.List;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, String> {
    // Optional custom finder
    List<Deposit> findByTdate(String tdate);
}
