package com.customer_rewards.rewards_calculation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;


@Entity
@Table(name = "customerSpendRewards")
@Data
public class CustomerRewards {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(name = "purchaseAmount")
        private Integer purchaseAmount;

        @Column(name = "purchase_date", columnDefinition = "DATE DEFAULT CURRENT_DATE")
        private Date purchaseDate;

        @Column(name = "rewards_points")
        private Double rewardsPoints;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "CUSTOMER_ID", nullable = false)
        private Customer customer;

    }
