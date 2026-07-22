package com.example.demo.repository;

import com.example.demo.dto.PaymentCardDisplayDto;
import com.example.demo.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
    JpaSpecificationExecutor<PaymentCard> {
    @Query("SELECT p from PaymentCard as p where p.user.id= :userId")
    List<PaymentCard> findByUserId(Long userId);
    @Query(value = """
        SELECT *
        FROM payment_cards
        WHERE user_id = :userId
        AND active = true
        """, nativeQuery = true)
    List<PaymentCard> findActiveCardsByUserId(Long userId);

    long countByUserId(Long userId);
}
