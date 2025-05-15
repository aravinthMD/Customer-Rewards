package com.customer_rewards.rewards_calculation.exception.customException;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
