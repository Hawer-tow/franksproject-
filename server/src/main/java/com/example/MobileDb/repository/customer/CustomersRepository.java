package com.example.MobileDb.repository.customer;

import com.example.MobileDb.entity.customer.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, Integer> {
}
