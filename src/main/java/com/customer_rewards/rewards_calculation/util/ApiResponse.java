package com.customer_rewards.rewards_calculation.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;
    private long timestamp;

    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }


}
