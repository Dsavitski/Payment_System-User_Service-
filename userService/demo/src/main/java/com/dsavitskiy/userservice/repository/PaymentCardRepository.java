package com.dsavitskiy.userservice.repository;

import com.dsavitskiy.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
    JpaSpecificationExecutor<PaymentCard> {
    @Query("SELECT p from PaymentCard as p where p.user.id= :userId")
    List<PaymentCard> findByUserId(UUID userId);
    @Query(value = """
        SELECT *
        FROM payment_cards
        WHERE user_id = :userId
        AND active = true
        """, nativeQuery = true)
    List<PaymentCard> findActiveCardsByUserId(UUID userId);

    long countByUserId(UUID userId);
}
