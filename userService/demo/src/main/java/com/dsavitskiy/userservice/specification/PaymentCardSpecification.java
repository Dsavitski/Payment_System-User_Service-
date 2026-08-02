package com.dsavitskiy.userservice.specification;

import com.dsavitskiy.userservice.entity.PaymentCard;
import com.dsavitskiy.userservice.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;


public class PaymentCardSpecification {

    private PaymentCardSpecification() {

    }

    public static Specification<PaymentCard> withFilters(String firstName, String surname) {
        return (root, query, cb) -> {
            Join<PaymentCard, User> userJoin = root.join("user");
            var predicate = cb.conjunction();
            if (firstName != null && !firstName.isBlank()) {
                predicate = cb.and(predicate, cb.like(cb.lower(userJoin.get("name")),
                    "%" + firstName.toLowerCase() + "%"));
            }
            if (surname != null && !surname.isBlank()) {
                predicate = cb.and(
                    predicate,
                    cb.like(cb.lower(userJoin.get("surname")), "%" + surname.toLowerCase() + "%")
                );
            }
            return predicate;
        };
    }
}