package com.grabit.repository;

import com.grabit.bean.EarningsDTO;
import com.grabit.entity.Earnings;
import com.grabit.enums.DeliveryPartnerOrderSpecificStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EarningsRepository extends JpaRepository<Earnings,Long> {
    Optional<Earnings> findByOrderId(Long orderId);
}
