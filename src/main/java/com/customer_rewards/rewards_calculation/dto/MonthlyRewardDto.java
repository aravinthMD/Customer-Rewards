package com.customer_rewards.rewards_calculation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyRewardDto {
    private Integer month;
    private Double rewardPoints;

}
