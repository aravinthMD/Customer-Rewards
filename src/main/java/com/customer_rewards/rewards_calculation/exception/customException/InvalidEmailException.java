package com.customer_rewards.rewards_calculation.exception.customException;


public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}