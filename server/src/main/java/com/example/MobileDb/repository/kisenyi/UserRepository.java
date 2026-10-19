package com.example.MobileDb.repository.kisenyi;

import com.example.MobileDb.entity.kisenyi.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    
    Optional<User> findById(Integer id);
}
