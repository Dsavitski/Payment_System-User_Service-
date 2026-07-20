package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "payment_cards")
public class PaymentCard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "number", nullable = false, unique = true)
    private String number;
    @Column(name = "holder", nullable = false)
    private String holder;
    @Column(name = "expiration_date")
    private LocalDate expirationDate;
    @Column(name = "active")
    private boolean active;

}
