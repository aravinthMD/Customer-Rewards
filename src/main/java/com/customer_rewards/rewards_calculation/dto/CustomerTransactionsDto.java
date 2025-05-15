package com.customer_rewards.rewards_calculation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CustomerTransactionsDto {

    private Long customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private List<MonthlyRewardDto> monthlyRecords;
    private Double totalRewards;

    public CustomerTransactionsDto() {

    }
}
