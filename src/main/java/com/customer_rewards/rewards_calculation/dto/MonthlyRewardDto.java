package com.customer_rewards.rewards_calculation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;

@Data
@AllArgsConstructor
public class MonthlyRewardDto {
    private Date purchaseDate;
    private Double rewardPoints;



    public MonthlyRewardDto() {

    }
}
