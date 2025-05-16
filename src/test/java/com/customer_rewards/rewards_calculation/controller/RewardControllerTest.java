package com.customer_rewards.rewards_calculation.controller;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.exception.customException.CustomerNotFoundException;
import com.customer_rewards.rewards_calculation.exception.customException.NullArgumentException;
import com.customer_rewards.rewards_calculation.service.RewardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardController.class)
@DisplayName("Reward Controller Test Suite")
public class RewardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RewardService customerRewardService;

    /**
     * Tests that a valid customer creation request returns a successful response.
     *
     * @throws Exception if an error occurs during the request execution.
     */
    @Test
    @DisplayName("Should successfully create a customer and return success status with customer data")
    public void testCreateCustomer_success() throws Exception {
        // Given
        CustomerRequestDto customerRequestDto = new CustomerRequestDto();
        customerRequestDto.setFirstName("John");
        customerRequestDto.setLastName("Wayne");
        customerRequestDto.setEmail("john.wayned@gmail.com");
        customerRequestDto.setPhone("8754809950");
        customerRequestDto.setAddress("123, Norther Park Street");

        CustomerResponseDto customerResponseDto = new CustomerResponseDto();
        customerResponseDto.setId(1L);
        customerResponseDto.setFirstName("John");
        customerResponseDto.setLastName("Wayne");
        customerResponseDto.setEmail("john.wayned@gmail.com");
        customerResponseDto.setPhone("8754809950");
        customerResponseDto.setAddress("123, Norther Park Street");

        when(customerRewardService.createCustomer(any(CustomerRequestDto.class)))
                .thenReturn(customerResponseDto);

        String requestBody = objectMapper.writeValueAsString(customerRequestDto);

        // When & Then
        mockMvc.perform(post("/api/saveCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.email").value("john.wayned@gmail.com"))
                .andExpect(jsonPath("$.message").value("Customer created successfully."));
    }

    /**
     * Tests that a customer creation attempt with invalid input (missing first name)
     * results in a Bad Request (400) response.
     *
     * @throws Exception if an error occurs during the request execution.
     */
    @Test
    @DisplayName("Should return BadRequest when creating a customer with missing first name")
    public void testCreateCustomer_invalidInput() throws Exception {
        // Given
        CustomerRequestDto invalidRequest = new CustomerRequestDto();
        invalidRequest.setLastName("Wayne");
        invalidRequest.setAddress("123, Northern Park Street");

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        when(customerRewardService.createCustomer(any(CustomerRequestDto.class)))
                .thenThrow(new NullArgumentException("First name cannot be empty"));

        // When & Then
        mockMvc.perform(post("/api/saveCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests a successful customer update via a PUT request.
     *
     * @throws Exception if an error occurs during request execution.
     */
    @Test
    @DisplayName("Should successfully update a customer and return a success status with updated details")
    public void testUpdateCustomer_success() throws Exception {
        // Given
        long customerId = 1L;

        CustomerRequestDto customerRequestDto = new CustomerRequestDto();
        customerRequestDto.setFirstName("John");
        customerRequestDto.setLastName("Wayne");
        customerRequestDto.setEmail("john.wayned@gmail.com");
        customerRequestDto.setPhone("8754809950");

        CustomerResponseDto customerResponseDto = new CustomerResponseDto();
        customerResponseDto.setId(1L);
        customerResponseDto.setFirstName("John");
        customerResponseDto.setLastName("Mathew");
        customerResponseDto.setEmail("john.mathew@gmail.com");
        customerResponseDto.setPhone("8754809950");
        customerResponseDto.setAddress("123, Norther Park Street");

        when(customerRewardService.updateCustomer(eq(customerId), any(CustomerRequestDto.class)))
                .thenReturn(customerResponseDto);

        String requestBody = objectMapper.writeValueAsString(customerRequestDto);

        // When & Then
        mockMvc.perform(put("/api/" + customerId + "/updateCustomer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                // The numeric value is compared as an integer.
                .andExpect(jsonPath("$.data.id").value((int) customerId))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.address").value("123, Norther Park Street"))
                .andExpect(jsonPath("$.message").value("Customer Updated successfully."));

    }

    /**
     * Tests that a customer update with invalid input returns a Bad Request (400) status.
     *
     * @throws Exception if an error occurs during request execution.
     */
    @Test
    @DisplayName("Should return BadRequest when updating a customer with invalid input (missing first name)")
    public void testUpdateCustomer_invalidInput() throws Exception {
        //Given
        long customerId = 1L;

        CustomerRequestDto invalidRequest = new CustomerRequestDto();
        invalidRequest.setLastName("Wayne");
        invalidRequest.setAddress("123, Northern Park Street");

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        when(customerRewardService.updateCustomer(eq(customerId), any(CustomerRequestDto.class)))
                .thenThrow(new NullArgumentException("First name cannot be empty"));


        // When & Then
        mockMvc.perform(put("/api/{customerId}/updateCustomer", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests that an attempt to update a non-existent customer returns a 404 Not Found status
     * along with the appropriate error message.
     *
     * @throws Exception if an error occurs during request execution.
     */
    @Test
    @DisplayName("Should return NotFound status and error message when updating a non-existing customer")
    public void testUpdateCustomer_customerNotAvailable() throws Exception {

        //Given
        long nonExistingCustomerId = 999L;

        CustomerRequestDto customerRequestDto = new CustomerRequestDto();
        customerRequestDto.setFirstName("John");
        customerRequestDto.setLastName("Wayne");
        customerRequestDto.setEmail("john.wayned@gmail.com");
        customerRequestDto.setPhone("8754809950");

        when(customerRewardService.updateCustomer(eq(nonExistingCustomerId), any(CustomerRequestDto.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: " + nonExistingCustomerId));

        String requestBody = objectMapper.writeValueAsString(customerRequestDto);

        // When & Then
        mockMvc.perform(put("/api/{customerId}/updateCustomer", nonExistingCustomerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer not found with id: " + nonExistingCustomerId));
    }

    /**
     * Tests that creating rewards for a customer returns a success response
     * with the expected transaction details.
     *
     * @throws Exception if an error occurs during the test execution.
     */
    @Test
    @DisplayName("Should successfully create rewards and return the transaction response with success status")
    public void testCreateRewards_success() throws Exception {
        //Given
        long customerId = 123L;

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setPurchaseAmount(100);
        transactionRequestDto.setPurchaseDate(Date.valueOf("2025-05-15"));

        TransactionResponseDto transactionResponseDto = new TransactionResponseDto();
        transactionResponseDto.setPurchaseDate(Date.valueOf("2025-05-15"));
        transactionResponseDto.setPurchaseAmount(100);
        transactionResponseDto.setRewardPoints(50.0);

        when(customerRewardService.createRewards(eq(customerId), any(TransactionRequestDto.class)))
                .thenReturn(transactionResponseDto);

        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When & Then
        mockMvc.perform(post("/api/{customerId}/saveReward", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.purchaseDate").value("2025-05-15"))
                .andExpect(jsonPath("$.data.purchaseAmount").value(100.0))
                .andExpect(jsonPath("$.data.rewardPoints").value(50.0))
                .andExpect(jsonPath("$.message").value("Transactions Saved successfully."));
    }

    /**
     * Tests that providing an invalid purchase amount results in a Bad Request (400) response.
     *
     * @throws Exception if an error occurs during the request execution.
     */
    @Test
    @DisplayName("Should return BadRequest when creating rewards with an invalid purchase amount")
    public void testCreateRewards_invalidPurchaseAmount() throws Exception {

        //Given
        long customerId = 123L;

        TransactionRequestDto transactionRequestDto = new TransactionRequestDto();
        transactionRequestDto.setPurchaseAmount(-1);
        transactionRequestDto.setPurchaseDate(new Date(15-05-2025));

        String requestBody = objectMapper.writeValueAsString(transactionRequestDto);

        // When & Then
        mockMvc.perform(post("/api/{customerId}/saveReward", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    /**
     * Tests that the GET endpoint for monthly transactions returns the expected customer transactions data.
     *
     * @throws Exception if an error occurs during request execution.
     */
    @Test
    @DisplayName("Should successfully retrieve monthly transactions with correct rewards details")
    public void testGetMonthlyTransactions_success() throws Exception {

        //Given
        long customerId = 123L;

        CustomerTransactionsDto customerTransactionsDto = new CustomerTransactionsDto();
        customerTransactionsDto.setCustomerId(customerId);
        customerTransactionsDto.setTotalRewards(200.0);

        List<MonthlyRewardDto> monthlyRewardDtoList = new ArrayList<>();
        MonthlyRewardDto monthlyRewardDto1 = new MonthlyRewardDto();
        monthlyRewardDto1.setPurchaseDate(new Date(15-05-2025));
        monthlyRewardDto1.setRewardPoints(150.0);
        monthlyRewardDtoList.add(monthlyRewardDto1);
        MonthlyRewardDto monthlyRewardDto2 = new MonthlyRewardDto();
        monthlyRewardDto2.setPurchaseDate(new Date(15-04-2025));
        monthlyRewardDto2.setRewardPoints(100.0);
        monthlyRewardDtoList.add(monthlyRewardDto2);

        customerTransactionsDto.setMonthlyRecords(monthlyRewardDtoList);

        when(customerRewardService.getCustomerMonthlyRewards(eq(customerId)))
                .thenReturn(customerTransactionsDto);

        // When & Then
        mockMvc.perform(get("/api/{customerId}/monthly-transactions", customerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value((int) customerId))
                .andExpect(jsonPath("$.totalRewards").value(200.0))
                .andExpect(jsonPath("$.monthlyRecords.length()").value(2));
    }

    /**
     * Tests that a GET request for monthly transactions with a non-existing customer ID
     * returns a 404 Not Found status and the appropriate error message.
     *
     * @throws Exception if an error occurs during request execution.
     */
    @Test
    @DisplayName("Should return NotFound and an error message when monthly transactions are requested for a non-existing customer")
    public void testGetMonthlyTransactions_customerNotFound() throws Exception {
        long nonExistingCustomerId = 999L;

        //Given
        when(customerRewardService.getCustomerMonthlyRewards(eq(nonExistingCustomerId)))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: " + nonExistingCustomerId));

        // When & Then
        mockMvc.perform(get("/api/{customerId}/monthly-transactions", nonExistingCustomerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer not found with id: " + nonExistingCustomerId));
    }

}
