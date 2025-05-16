package com.customer_rewards.rewards_calculation.service;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.entity.Customer;
import com.customer_rewards.rewards_calculation.entity.CustomerRewards;
import com.customer_rewards.rewards_calculation.exception.customException.*;
import com.customer_rewards.rewards_calculation.repository.CustomerRepository;
import com.customer_rewards.rewards_calculation.repository.CustomerRewardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reward Service Test Suite")
public class RewardsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerRewardRepository customerRewardRepository;

    @InjectMocks
    private RewardService rewardService;

    /**
     * Tests successful creation of a reward transaction.
     * Ensures that rewards are correctly calculated and saved.
     */
    @Test
    @DisplayName("Should create rewards successfully and return a valid TransactionResponseDto")
    public void testCreateRewards_Success() {

        //Given
        Long customerId = 1L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPurchaseAmount(100);
        requestDto.setPurchaseDate(Date.valueOf("2025-05-10"));

        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        CustomerRewards mockRewards = new CustomerRewards();
        mockRewards.setCustomer(mockCustomer);
        mockRewards.setPurchaseAmount(requestDto.getPurchaseAmount());
        mockRewards.setRewardsPoints(50.0);
        mockRewards.setPurchaseDate(requestDto.getPurchaseDate());

        when(customerRewardRepository.save(any())).thenReturn(mockRewards);

        //When
        TransactionResponseDto response = rewardService.createRewards(customerId, requestDto);

        //Then
        assertNotNull(response);
        assertEquals(requestDto.getPurchaseAmount(), response.getPurchaseAmount());
        assertEquals(50.0, response.getRewardPoints());
    }

    /**
     * Tests auto-generation of purchase date when null.
     * Ensures that a default purchase date is correctly assigned.
     */
    @Test
    @DisplayName("Create Rewards: When purchase date is null, set default date and calculate rewards correctly")
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

    /**
     * Tests handling when the customer does not exist.
     * Expects an exception when customer is not found in the repository.
     */
    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer is not found during reward creation")
    public void testCreateRewards_CustomerNotFound() {
        // Given
        Long customerId = 2L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPurchaseAmount(120);
        requestDto.setPurchaseDate(null);

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, () -> {
            rewardService.createRewards(customerId, requestDto);
        });
        assertEquals("Customer not found with ID: "+customerId, exception.getMessage(), "Expected 'Customer not found' message");
    }

    /**
     * Tests handling of a null customer ID.
     * Expects an exception when customer ID is not provided.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when customer ID is null during reward creation")
    public void testCreateRewards_NullCustomerId_ShouldThrowException() {

        //When
        Exception exception = assertThrows(NullArgumentException.class, () ->
                rewardService.createRewards(null, new TransactionRequestDto()));

        //Then
        assertEquals("Customer ID cannot be null.", exception.getMessage());
    }

    /**
     * Tests handling of a null transaction request.
     * Expects an exception when transaction request is missing.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when TransactionRequest is null during reward creation")
    public void testCreateRewards_NullTransactionRequest_ShouldThrowException() {

        //Given
        Long customerId = 1L;

        //When
        Exception exception = assertThrows(NullArgumentException.class, () ->
                rewardService.createRewards(customerId, null));
        //Then
        assertEquals("Customer Transaction request cannot be null.", exception.getMessage());
    }

    /**
     * Tests handling of a null purchase amount.
     * Expects an exception when purchase amount is not provided.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when purchase amount is null")
    public void testCreateRewards_NullPurchaseAmount_ShouldThrowException() {

        //Given
        Long customerId = 1L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPurchaseAmount(null);

        //When
        Exception exception = assertThrows(NullArgumentException.class, () ->
                rewardService.createRewards(customerId, requestDto));

        //Then
        assertEquals("Customer Purchase Amount cannot be null.", exception.getMessage());
    }

    /**
     * Tests handling of zero or negative purchase amounts.
     * Expects an exception when purchase amount is invalid.
     */
    @Test
    @DisplayName("Should throw InvalidPurchaseAmountException when purchase amount is zero")
    public void testCreateRewards_InvalidPurchaseAmount_ShouldThrowException() {

        //Given
        Long customerId = 1L;
        TransactionRequestDto requestDto = new TransactionRequestDto();
        requestDto.setPurchaseAmount(0);

        //When && Then
        Exception exception = assertThrows(InvalidPurchaseAmountException.class, () ->
                rewardService.createRewards(customerId, requestDto));

        assertTrue(exception.getMessage().contains("Purchase amount must be greater than zero"));
    }

    /**
     * Tests successful retrieval of monthly transactions.
     */
    @Test
    @DisplayName("Should retrieve valid monthly rewards for customer from recent transactions")
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

        List<CustomerRewards> transactions = Arrays.asList(
                createMockTransaction(date1.toString(), 150),
                createMockTransaction(date2.toString(), 200),
                createMockTransaction(oldDate.toString(), 200)
        );
        customer.setTransactions(transactions);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // When
        CustomerTransactionsDto transactionsDto = rewardService.getCustomerMonthlyRewards(customerId);

        //Then
        assertNotNull(transactionsDto, "Returned DTO should not be null");
        assertEquals(customer.getId(), transactionsDto.getCustomerId());
        assertEquals(customer.getFirstName(), transactionsDto.getFirstName());

        double expectedTotalRewards = 150.0 + 250.0;  // = 400.0
        assertEquals(expectedTotalRewards, transactionsDto.getTotalRewards(), 0.001);

        List<MonthlyRewardDto> monthlyRecords = transactionsDto.getMonthlyRecords();

        assertEquals(2, monthlyRecords.size(), "Should have 2 monthly records");

    }

    /**
     * Tests handling when the customer does not exist.
     * Expects an exception when customer is not found in the repository.
     */
    @Test
    @DisplayName("Should throw CustomerNotFoundException when customer is not found")
    public void testGetCustomerMonthlyRewards_CustomerNotFound() {
        // given
        Long customerId = 5L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // when & then
        Exception ex = assertThrows(CustomerNotFoundException.class, () -> {
            rewardService.getCustomerMonthlyRewards(customerId);
        });
        assertEquals("Customer not found with ID: " + customerId, ex.getMessage());
    }

    /**
     * Tests handling of a null customer ID.
     * Expects an exception when customer ID is missing.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when customer ID is null")
    public void testGetCustomerMonthlyRewards_NullCustomerId_ShouldThrowException() {

        //When && Then
        Exception exception = assertThrows(NullArgumentException.class, () ->
                rewardService.getCustomerMonthlyRewards(null));

        assertEquals("Customer ID cannot be null.", exception.getMessage());
    }

    /**
     * Tests handling when the customer has no transaction history.
     * Expects a valid response with empty monthly records and zero reward points.
     */
    @Test
    @DisplayName("Should return empty rewards details when customer has no transactions")
    public void testGetCustomerMonthlyRewards_NoTransactions() {

        //Given
        Long customerId = 1L;
        Customer mockCustomer = new Customer();
        mockCustomer.setId(customerId);
        mockCustomer.setTransactions(Collections.emptyList());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        //When
        CustomerTransactionsDto response = rewardService.getCustomerMonthlyRewards(customerId);

        //Then
        assertNotNull(response);
        assertTrue(response.getMonthlyRecords().isEmpty());
        assertEquals(0, response.getTotalRewards());
    }

    /**
     * Tests aggregation of monthly reward transactions.
     * Ensures correct mapping and filtering logic for rewards per month.
     */
    @Test
    @DisplayName("Should correctly aggregate monthly rewards for customer transactions")
    public void testGetCustomerMonthlyRewards_AggregationValidation() {

        //Given
        Long customerId = 1L;
        Customer mockCustomer = createMockCustomerWithTransactions(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        //When
        CustomerTransactionsDto response = rewardService.getCustomerMonthlyRewards(customerId);

        //Then
        assertEquals(2, response.getMonthlyRecords().size());
        assertTrue(response.getTotalRewards() > 0);
    }

    private Customer createMockCustomerWithTransactions(Long customerId) {

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("John");
        customer.setLastName("Nathan");
        customer.setEmail("john.n@gmail.com");
        customer.setPhone("8754809950");
        customer.setAddress("123 Crawford Ave");

        List<CustomerRewards> transactions = Arrays.asList(
                createMockTransaction("2025-04-15", 150),
                createMockTransaction("2025-03-10", 200)
        );

        customer.setTransactions(transactions);
        return customer;
    }

    private CustomerRewards createMockTransaction(String dateString, int purchaseAmount) {
        LocalDate localDate = LocalDate.parse(dateString);
        Date sqlDate = Date.valueOf(localDate);

        CustomerRewards rewards = new CustomerRewards();
        rewards.setPurchaseDate(sqlDate);
        rewards.setPurchaseAmount(purchaseAmount);
        rewards.setRewardsPoints(calculateRewardPoints(purchaseAmount));
        return rewards;
    }

    private double calculateRewardPoints(double purchaseAmount) {
        double points = 0;

        if (purchaseAmount > 100) {
            points += (purchaseAmount - 100) * 2;
            purchaseAmount = 100;
        }

        if (purchaseAmount > 50) {
            points += (purchaseAmount - 50) * 1;
        }

        return points;
    }

    /**
     * Tests that passing a null CustomerRequestDto results in a {@code NullArgumentException}.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when attempting to create a customer with a null request")
    public void testCreateCustomer_NullRequest_ShouldThrowException() {

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.createCustomer(null);
        });
    }

    /**
     * Tests that an empty first name (blank string) throws a {@code NullArgumentException}.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when attempting to create a customer with an empty first name")
    public void testCreateCustomer_EmptyFirstName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("  ");
        request.setLastName("Wayne");
        request.setEmail("wayne@gmail.com");
        request.setPhone("8754809960");
        request.setAddress("123, Northern Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that a first name with invalid characters (e.g., containing digits) throws an {@code InvalidPatternException}.
     */
    @Test
    @DisplayName("Should throw InvalidPatternException when first name contains invalid characters")
    public void testCreateCustomer_InvalidFirstName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John3");
        request.setLastName("Wayne");
        request.setEmail("johnwayne@gmail.com");
        request.setPhone("8754809960");
        request.setAddress("23,Northern Park Street");

        //When && Then
        assertThrows(InvalidPatternException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that an empty last name (blank string) causes a {@code NullArgumentException}.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when customer's last name is empty")
    public void testCreateCustomer_EmptyLastName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("");
        request.setEmail("john@gmail.com");
        request.setPhone("8754809960");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that a last name with invalid characters throws an {@code InvalidPatternException}.
     */
    @Test
    @DisplayName("Should throw InvalidPatternException when last name contains invalid characters")
    public void testCreateCustomer_InvalidLastName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne3");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("8754809940");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(InvalidPatternException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that an empty email (blank string) triggers a {@code NullArgumentException}.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when email is empty or blank")
    public void testCreateCustomer_EmptyEmail_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("   ");
        request.setPhone("8754809960");
        request.setAddress("123,Northen Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that an invalid email format triggers an {@code InvalidEmailException}.
     */
    @Test
    @DisplayName("Should throw InvalidEmailException when email format is invalid")
    public void testCreateCustomer_InvalidEmailFormat_ShouldThrowException() {

        //When
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe");
        request.setPhone("8754809960");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(InvalidEmailException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that an empty phone number throws a {@code NullArgumentException}.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when phone is empty or blank")
    public void testCreateCustomer_EmptyPhone_ShouldThrowException() {

        //given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPhone("    ");
        request.setAddress("Some Address");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.createCustomer(request);
        });
    }

    /**
     * Tests that a phone number not matching the required 10-digit format throws an {@code InvalidPhoneException}.
     */
    @Test
    @DisplayName("Should throw InvalidPhoneException when the phone number format is invalid")
    public void testCreateCustomer_InvalidPhoneFormat_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@gmail.com");
        request.setPhone("8754");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(InvalidPhoneException.class, () -> {
            rewardService.createCustomer(request);
        });
    }


    /**
     * Tests that if the User already exists in the repository, a {@code UserAlreadyExistsException} is thrown.
     */
    @Test
    @DisplayName("Should throw UserAlreadyExistsException when creating a customer with an existing email")
    public void testCreateCustomer_UserAlreadyExists_ShouldThrowException() {
        // Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("8754809960");
        request.setAddress("123,Northern Park Street");

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When & Then
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, () -> {
            rewardService.createCustomer(request);
        });
        assertEquals("Customer with email john.wayne@gmail.com already exists.", exception.getMessage());
    }

    /**
     * Tests that a valid {@code CustomerRequestDto} results in successfully creating a customer,
     * and that the returned {@code CustomerResponseDto} is properly populated.
     */
    @Test
    @DisplayName("Should successfully create a customer and return a valid CustomerResponseDto")
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

    /**
     * Verifies that if a null {@code customerId} is provided, a {@code NullArgumentException} is thrown.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when updating a customer with a null ID")
    public void testUpdateCustomer_NullCustomerId_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("8754809960");
        request.setAddress("123,Northern Park Street");

        //When
        NullArgumentException exception = assertThrows(NullArgumentException.class, () -> {
            rewardService.updateCustomer(null, request);
        });

        //Then
        assertEquals("Customer ID cannot be null.", exception.getMessage());
    }

    /**
     * Verifies that if a null {@code CustomerRequestDto} is provided, a {@code NullArgumentException} is thrown.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when updating a customer with a null request")
    public void testUpdateCustomer_NullCustomerRequest_ShouldThrowException() {

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.updateCustomer(1L, null);
        });
    }

    /**
     * Verifies that if the first name is empty (only whitespace), a {@code NullArgumentException} is thrown.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when updating a customer with an empty first name")
    public void testUpdateCustomer_EmptyFirstName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("   ");
        request.setLastName("Wayne");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("8754809960");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the first name does not match the validation pattern (e.g., contains digits),
     * an {@code InvalidPatternException} is thrown.
     */
    @Test
    @DisplayName("Should throw InvalidPatternException when updating a customer with an invalid first name format")
    public void testUpdateCustomer_InvalidFirstName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John3");
        request.setLastName("Wayne");
        request.setEmail("john.wayne@example.com");
        request.setPhone("8754809950");
        request.setAddress("123,Northen Park Street");

        //When && Then
        assertThrows(InvalidPatternException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the last name is empty, a {@code NullArgumentException} is thrown.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when updating a customer with an empty last name")
    public void testUpdateCustomer_EmptyLastName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("");
        request.setEmail("john@gmail.com");
        request.setPhone("8754809950");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the last name does not match the required pattern,
     * an {@code InvalidPatternException} is thrown.
     */
    @Test
    @DisplayName("Should throw InvalidPatternException when updating a customer with an invalid last name format")
    public void testUpdateCustomer_InvalidLastName_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wyne3");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("8754809950");
        request.setAddress("123,Northern Park Street");

        //When && Then
        assertThrows(InvalidPatternException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the email field is empty, a {@code NullArgumentException} is thrown.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when updating a customer with an empty or blank email")
    public void testUpdateCustomer_EmptyEmail_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("   ");
        request.setPhone("8754809950");
        request.setAddress("123,Northen Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the email format is invalid, an {@code InvalidEmailException} is thrown.
     */
    @Test
    @DisplayName("Should throw InvalidEmailException when updating a customer with an invalid email format")
    public void testUpdateCustomer_InvalidEmailFormat_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("john.wayne");
        request.setPhone("8754809950");
        request.setAddress("123,Northen Park Street");

        //When && Then
        assertThrows(InvalidEmailException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the phone number is empty, a {@code NullArgumentException} is thrown.
     */
    @Test
    @DisplayName("Should throw NullArgumentException when updating a customer with an empty or blank phone number")
    public void testUpdateCustomer_EmptyPhone_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("wayne");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("   ");
        request.setAddress("123,Northen Park Street");

        //When && Then
        assertThrows(NullArgumentException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

    /**
     * Verifies that if the phone number length is not exactly 10 digits,
     * an {@code InvalidPhoneException} is thrown.
     */
    @Test
    @DisplayName("Should throw InvalidPhoneException when updating a customer with an invalid phone format")
    public void testUpdateCustomer_InvalidPhoneFormat_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("john.wayne@gmail.com");
        request.setPhone("87549");
        request.setAddress("123,Northen Park Street");

        //When && Then
        assertThrows(InvalidPhoneException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }


    /**
     * Verifies that a valid update request updates the customer details correctly,
     * and a properly populated {@code CustomerResponseDto} is returned.
     */
    @Test
    @DisplayName("Should successfully update a customer and return the updated CustomerResponseDto")
    public void testUpdateCustomer_Success() {
        // Given
        Long customerId = 1L;

        Customer existingCustomer = new Customer();
        existingCustomer.setId(customerId);
        existingCustomer.setFirstName("Henry");
        existingCustomer.setLastName("Jose");
        existingCustomer.setEmail("henry.jo@gmail.com");
        existingCustomer.setPhone("8754808754");
        existingCustomer.setAddress("123, Park Street, California");

        CustomerRequestDto updateDto = new CustomerRequestDto();
        updateDto.setFirstName("Mark");
        updateDto.setLastName("Milan");
        updateDto.setEmail("henry.j@gmail.com");
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
        assertEquals("Milan", responseDto.getLastName(), "Last name should be updated");
        assertEquals("henry.j@gmail.com", responseDto.getEmail(), "Email should be updated");
        assertEquals("8754808754", responseDto.getPhone(), "Phone should be updated");
        assertEquals("123, Park Street, California, USA", responseDto.getAddress(), "Address should be updated");
    }

    /**
     * Verifies that attempting to update a non-existing customer results in a {@code CustomerNotFoundException}.
     */
    @Test
    @DisplayName("Should throw CustomerNotFoundException when updating a customer that does not exist")
    public void testUpdateCustomer_CustomerNotFound_ShouldThrowException() {

        //Given
        CustomerRequestDto request = new CustomerRequestDto();
        request.setFirstName("John");
        request.setLastName("Wayne");
        request.setEmail("john.wayne@example.com");
        request.setPhone("8754809950");
        request.setAddress("123,Northen Park Street");

        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        //When && Then
        assertThrows(CustomerNotFoundException.class, () -> {
            rewardService.updateCustomer(1L, request);
        });
    }

}
