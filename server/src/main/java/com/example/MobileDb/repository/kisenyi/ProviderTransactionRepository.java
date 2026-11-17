package com.example.MobileDb.repository.kisenyi;

import com.example.MobileDb.entity.kisenyi.ProviderTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderTransactionRepository extends JpaRepository<ProviderTransaction, Long> {

    Optional<ProviderTransaction> findByReference(String reference);

    Optional<ProviderTransaction> findFirstByProviderRequestId(String providerRequestId);

    boolean existsByReference(String reference);
}
