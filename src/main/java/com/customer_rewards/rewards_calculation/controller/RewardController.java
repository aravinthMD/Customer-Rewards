package com.customer_rewards.rewards_calculation.controller;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.service.RewardService;
import com.customer_rewards.rewards_calculation.util.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("api")
public class RewardController {

    @Autowired
    private RewardService customerRewardService;

    @PostMapping(path = "/saveCustomer")
    public ResponseEntity<ApiResponse<CustomerResponseDto>> createCustomer(@Valid @RequestBody CustomerRequestDto customerRequestDto) {

        CustomerResponseDto customerResponseDto = customerRewardService.createCustomer(customerRequestDto);
        ApiResponse<CustomerResponseDto> response = new ApiResponse<>(
                "success",
                customerResponseDto,
                "Customer created successfully.",
                System.currentTimeMillis()
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "{customerId}/updateCustomer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CustomerResponseDto>> updateCustomer(@NotNull(message = "Customer ID is Mandatory") @PathVariable long customerId,@Valid @RequestBody CustomerRequestDto customerRequestDto) {
        CustomerResponseDto updatedCustomer = customerRewardService.updateCustomer(customerId, customerRequestDto);

        ApiResponse<CustomerResponseDto> response = new ApiResponse<>(
                "success",
                updatedCustomer,
                "Customer Updated successfully.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(path = "{customerId}/saveReward",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<TransactionResponseDto>> createRewards(@NotNull(message = "Customer ID is Mandatory") @PathVariable long customerId, @Valid @RequestBody TransactionRequestDto transactionRequestDto) {

        TransactionResponseDto customerRewards = customerRewardService.createRewards(customerId,transactionRequestDto);
        ApiResponse<TransactionResponseDto> response = new ApiResponse<>(
                "success",
                customerRewards,
                "Transactions Saved successfully.",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @GetMapping("/{customerId}/monthly-transactions")
    public ResponseEntity<CustomerTransactionsDto> getMonthlyTransactions(@PathVariable Long customerId) {
        CustomerTransactionsDto result = customerRewardService.getCustomerMonthlyRewards(customerId);
        return ResponseEntity.ok(result);
    }

}


