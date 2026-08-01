package com.dsavitskiy.userservice.repository;

import com.dsavitskiy.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("""
    select u
    from User u
    left join fetch u.paymentCards
    where u.id = :id
    """)
    Optional<User> findUserWithPaymentCardsById(UUID id);
}
