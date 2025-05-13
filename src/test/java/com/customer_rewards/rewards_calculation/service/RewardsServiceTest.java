package com.customer_rewards.rewards_calculation.service;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.entity.Customer;
import com.customer_rewards.rewards_calculation.entity.CustomerRewards;
import com.customer_rewards.rewards_calculation.repository.CustomerRepository;
import com.customer_rewards.rewards_calculation.repository.CustomerRewardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RewardsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerRewardRepository customerRewardRepository;

    @InjectMocks
    private RewardService rewardService;

    @Test
    public void testCreateRewards_WithNullPurchaseDate_ShouldSetDefaultDateAndCalculateRewards() {
        // Given

        Long customerId = 1L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPurchaseAmount(180);
        requestDto.setPurchaseDate(null);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Aravinth");
        customer.setLastName("MD");
        customer.setEmail("aravinth.md@gmail.com");
        customer.setPhone("8754809950");
        customer.setAddress("1/2, northern street, Ohio");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        when(customerRewardRepository.save(any(CustomerRewards.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionResponseDto response = rewardService.createRewards(customerId, requestDto);

        // Then
        assertNotNull(response);
        assertEquals(180, response.getPurchaseAmount().intValue());

        assertNotNull(response.getPurchaseDate());

        assertEquals(210.0, response.getRewardPoints());

        CustomerResponseDto customerResp = response.getCustomerResponseDto();
        assertNotNull(customerResp);
        assertEquals(1L, customerResp.getId());
        assertEquals("Aravinth", customerResp.getFirstName());
        assertEquals("MD", customerResp.getLastName());
        assertEquals("aravinth.md@gmail.com", customerResp.getEmail());
        assertEquals("8754809950", customerResp.getPhone());
        assertEquals("1/2, northern street, Ohio", customerResp.getAddress());
    }

    @Test
    public void testCreateRewards_CustomerNotFound() {
        // Given
        Long customerId = 2L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPurchaseAmount(120);
        requestDto.setPurchaseDate(null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rewardService.createRewards(customerId, requestDto);
        });
        assertEquals("Customer not found", exception.getMessage(), "Expected 'Customer not found' message");
    }

    @Test
    public void testGetCustomerMonthlyRewards_success() {
        // Given
        Long customerId = 1L;

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Nathan");
        customer.setEmail("john.n@gmail.com");
        customer.setPhone("8754809950");
        customer.setAddress("123 Crawford Ave");

        LocalDate date1 = LocalDate.now();
        LocalDate date2 = LocalDate.now().minusMonths(1);

        LocalDate oldDate = LocalDate.now().minusMonths(4);

        CustomerRewards tx1 = new CustomerRewards();
        tx1.setPurchaseAmount(150);
        tx1.setPurchaseDate(Date.valueOf(date1));

        CustomerRewards tx2 = new CustomerRewards();
        tx2.setPurchaseAmount(200);
        tx2.setPurchaseDate(Date.valueOf(date2));

        CustomerRewards txOld = new CustomerRewards();
        txOld.setPurchaseAmount(500);
        txOld.setPurchaseDate(Date.valueOf(oldDate));

        customer.setTransactions(Arrays.asList(tx1, tx2, txOld));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        CustomerTransactionsDto transactionsDto = rewardService.getCustomerMonthlyRewards(customerId);

        //Then
        assertNotNull(transactionsDto, "Returned DTO should not be null");
        assertEquals(customer.getId(), transactionsDto.getCustomerId());
        assertEquals(customer.getFirstName(), transactionsDto.getFirstName());
        assertEquals(customer.getLastName(), transactionsDto.getLastName());
        assertEquals(customer.getEmail(), transactionsDto.getEmail());
        assertEquals(customer.getPhone(), transactionsDto.getPhone());
        assertEquals(customer.getAddress(), transactionsDto.getAddress());

        double expectedTotalRewards = 150.0 + 250.0;  // = 400.0
        assertEquals(expectedTotalRewards, transactionsDto.getTotalRewards(), 0.001);

        List<MonthlyRewardDto> monthlyRecords = transactionsDto.getMonthlyRecords();

        assertEquals(2, monthlyRecords.size(), "Should have 2 monthly records");

    }

    @Test
    public void testGetCustomerMonthlyRewards_CustomerNotFound() {
        // given
        Long customerId = 5L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            rewardService.getCustomerMonthlyRewards(customerId);
        });
        assertEquals("Customer not found with ID: " + customerId, ex.getMessage());
    }

    @Test
    public void testCreateCustomer_Success() {

        //Given
        CustomerRequestDto requestDto = new CustomerRequestDto();
        requestDto.setFirstName("John");
        requestDto.setLastName("Oliver");
        requestDto.setEmail("john.o@gmail.com");
        requestDto.setPhone("8769877890");
        requestDto.setAddress("123 Main Street");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setFirstName(requestDto.getFirstName());
        savedCustomer.setLastName(requestDto.getLastName());
        savedCustomer.setEmail(requestDto.getEmail());
        savedCustomer.setPhone(requestDto.getPhone());
        savedCustomer.setAddress(requestDto.getAddress());

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When
        CustomerResponseDto responseDto = rewardService.createCustomer(requestDto);

        // Then
        assertNotNull(responseDto, "Response should not be null");
        assertEquals(1L, responseDto.getId(), "Customer ID should be 1");
        assertEquals("John", responseDto.getFirstName(), "First name should match");
        assertEquals("Oliver", responseDto.getLastName(), "Last name should match");
        assertEquals("john.o@gmail.com", responseDto.getEmail(), "Email should match");
        assertEquals("8769877890", responseDto.getPhone(), "Phone should match");
        assertEquals("123 Main Street", responseDto.getAddress(), "Address should match");
    }

    @Test
    public void testUpdateCustomer_Success() {
        // Given
        Long customerId = 1L;

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setFirstName("Henry");
        existingCustomer.setLastName("Jose");
        existingCustomer.setEmail("henry.jo@example.com");
        existingCustomer.setPhone("8754808754");
        existingCustomer.setAddress("123, Park Street, California");

        CustomerRequestDto updateDto = new CustomerRequestDto();
        updateDto.setFirstName("Mark");
        updateDto.setLastName("Jose Milan");
        updateDto.setEmail("henry.j@example.com");
        updateDto.setPhone("8754808754");
        updateDto.setAddress("123, Park Street, California, USA");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerResponseDto responseDto = rewardService.updateCustomer(customerId, updateDto);

        // Then
        assertNotNull(responseDto, "The response should not be null");
        assertEquals(customerId, responseDto.getId(), "Customer ID should match");
        assertEquals("Mark", responseDto.getFirstName(), "First name should be updated");
        assertEquals("Jose Milan", responseDto.getLastName(), "Last name should be updated");
        assertEquals("henry.j@example.com", responseDto.getEmail(), "Email should be updated");
        assertEquals("8754808754", responseDto.getPhone(), "Phone should be updated");
        assertEquals("123, Park Street, California, USA", responseDto.getAddress(), "Address should be updated");
    }

    @Test
    public void testUpdateCustomer_CustomerNotFound() {
        // Given
        Long customerId = 1L;
        CustomerRequestDto updateDto = new CustomerRequestDto();
        updateDto.setFirstName("Foo");

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                rewardService.updateCustomer(customerId, updateDto)
        );
        assertEquals("Customer not found with ID: " + customerId, exception.getMessage());
    }




}
