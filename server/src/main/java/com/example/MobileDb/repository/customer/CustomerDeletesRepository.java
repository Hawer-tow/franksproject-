package com.example.MobileDb.repository.customer;

import com.example.MobileDb.entity.customer.CustomerDeletes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CustomerDeletesRepository extends JpaRepository<CustomerDeletes, Integer> {
}
