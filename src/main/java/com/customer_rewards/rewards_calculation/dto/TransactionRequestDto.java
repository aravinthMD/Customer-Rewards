package com.customer_rewards.rewards_calculation.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDto {

    @NotNull(message = "Purchase amount must not be null")
    @PositiveOrZero(message = "Purchase amount must be zero or positive")

    @Schema(description = "Purchase Amount of the Customer", example = "120")
    private Integer purchaseAmount;

    @NotNull(message = "PurchaseDate must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "The date and time of the purchase", example = "2025-05-13T10:38:41.188Z")
    private Date purchaseDate;

}
