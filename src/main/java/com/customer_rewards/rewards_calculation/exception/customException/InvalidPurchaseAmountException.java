package com.customer_rewards.rewards_calculation.exception.customException;

public class InvalidPurchaseAmountException extends RuntimeException {
    public InvalidPurchaseAmountException(String message) {
        super(message);
    }
}
