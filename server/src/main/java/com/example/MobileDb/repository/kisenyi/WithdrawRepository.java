package com.example.MobileDb.repository.kisenyi;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MobileDb.entity.kisenyi.Withdraw;

import java.util.List;

@Repository
public interface WithdrawRepository extends JpaRepository<Withdraw, String> {
    // Example custom finder if needed
    List<Withdraw> findByTdate(String tdate);
}
