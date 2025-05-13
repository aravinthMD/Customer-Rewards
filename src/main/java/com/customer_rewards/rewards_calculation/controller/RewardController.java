package com.customer_rewards.rewards_calculation.controller;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.service.RewardService;
import com.customer_rewards.rewards_calculation.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Operation(
            summary = "Create a Customer",
            description = "Creates a new Customer record with the provided details."
    )
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

    @Operation(
            summary = "Update the Customer",
            description = "Updates the Customer record with the provided details."
    )
    @PutMapping(path = "{customerId}/updateCustomer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CustomerResponseDto>> updateCustomer(
            @Parameter(
                    description = "Unique identifier of the Customer",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "123")
            )
            @NotNull(message = "Customer ID is Mandatory") @PathVariable long customerId,@Valid @RequestBody CustomerRequestDto customerRequestDto) {
        CustomerResponseDto updatedCustomer = customerRewardService.updateCustomer(customerId, customerRequestDto);

        ApiResponse<CustomerResponseDto> response = new ApiResponse<>(
                "success",
                updatedCustomer,
                "Customer Updated successfully.",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Save the Transaction",
            description = "Saves the Customer Transaction Details"
    )
    @PostMapping(path = "{customerId}/saveReward",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<TransactionResponseDto>> createRewards(
            @Parameter(
                    description = "Unique identifier of the Customer",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "123")
            )
            @NotNull(message = "Customer ID is Mandatory") @PathVariable long customerId, @Valid @RequestBody TransactionRequestDto transactionRequestDto) {

        TransactionResponseDto customerRewards = customerRewardService.createRewards(customerId,transactionRequestDto);
        ApiResponse<TransactionResponseDto> response = new ApiResponse<>(
                "success",
                customerRewards,
                "Transactions Saved successfully.",
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(
            summary = "Get Customer Transaction by ID",
            description = "Fetches the monthly Transaction Reward details using the provided unique customer ID."
    )
    @GetMapping("/{customerId}/monthly-transactions")
    public ResponseEntity<CustomerTransactionsDto> getMonthlyTransactions(
            @Parameter(
                    description = "Unique identifier of the customer",
                    required = true,
                    schema = @Schema(type = "integer", format = "int64", example = "123")
            )
            @PathVariable Long customerId) {
        CustomerTransactionsDto result = customerRewardService.getCustomerMonthlyRewards(customerId);
        return ResponseEntity.ok(result);
    }

}


