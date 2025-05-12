package com.customer_rewards.rewards_calculation.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;

@Data
@AllArgsConstructor
public class TransactionRequestDto {

    @NotNull(message = "Purchase amount must not be null")
    @PositiveOrZero(message = "Purchase amount must be zero or positive")
    private Integer purchaseAmount;

    @NotNull(message = "PurchaseDate must not be null")
    private Date PurchaseDate;

}
