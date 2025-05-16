package com.customer_rewards.rewards_calculation.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
public class MonthlyRewardDto {
    @JsonIgnore
    private Date purchaseDate;
    @JsonProperty("date")
    private String formattedDate;
    private Double rewardPoints;

    public MonthlyRewardDto(Date purchaseDate,LocalDate formatableDate, double rewardPoints) {
        this.purchaseDate = purchaseDate;
        this.formattedDate = formatableDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")); // Example: "April 25, 2025"
        this.rewardPoints = rewardPoints;
    }



    public MonthlyRewardDto() {

    }
}
