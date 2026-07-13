package com.poultry.broiler_farming_system.repository;

import com.poultry.broiler_farming_system.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}
