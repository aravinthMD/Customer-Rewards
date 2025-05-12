package com.customer_rewards.rewards_calculation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;

@Data
@AllArgsConstructor
public class TransactionResponseDto {

    private Integer purchaseAmount;

    private Date PurchaseDate;

    private Integer rewardPoints;

    @JsonProperty("customerDetail")
    private CustomerResponseDto customerResponseDto;
}
