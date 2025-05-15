package com.customer_rewards.rewards_calculation.service;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.entity.Customer;
import com.customer_rewards.rewards_calculation.entity.CustomerRewards;
import com.customer_rewards.rewards_calculation.exception.customException.*;
import com.customer_rewards.rewards_calculation.repository.CustomerRepository;
import com.customer_rewards.rewards_calculation.repository.CustomerRewardRepository;
import com.customer_rewards.rewards_calculation.util.constants.ValidationConstants;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardService {

    @Autowired
    private CustomerRewardRepository customerRewardRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Creates a new customer after validating the input details.
     *
     * @param customerRequestDto the customer request payload
     * @return the response DTO with the created customer details
     */
    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto){

        String pattern = "^[A-Za-z]+$";

        if (customerRequestDto == null) {
            throw new NullArgumentException("Customer request cannot be null.");
        }

        if (!StringUtils.hasText(customerRequestDto.getFirstName())) {
            throw new NullArgumentException("First name cannot be null or empty.");
        }
        if (!ValidationConstants.NAME_PATTERN.matcher(customerRequestDto.getFirstName()).matches()) {
            throw new InvalidPatternException("First name must contain only alphabets.");
        }
        if (!StringUtils.hasText(customerRequestDto.getLastName())) {
            throw new NullArgumentException("Last name cannot be null or empty.");
        }
        if (!ValidationConstants.NAME_PATTERN.matcher(customerRequestDto.getLastName()).matches()) {
            throw new InvalidPatternException("Last name must contain only alphabets.");
        }
        if (!StringUtils.hasText(customerRequestDto.getEmail())) {
            throw new NullArgumentException("Email cannot be null or empty.");
        }
        if (!StringUtils.hasText(customerRequestDto.getPhone())) {
            throw new NullArgumentException("Phone number cannot be null or empty.");
        }

        if (!customerRequestDto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new InvalidEmailException("Invalid email format: " + customerRequestDto.getEmail());
        }

        if (!customerRequestDto.getPhone().matches("^\\d{10}$")) {
            throw new InvalidPhoneException("Phone number must be exactly 10 digits.");
        }

        if (customerRepository.existsByEmail(customerRequestDto.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Customer with email " + customerRequestDto.getEmail() + " already exists."
            );
        }

        Customer customer = new Customer();
        customer.setFirstName(customerRequestDto.getFirstName());
        customer.setLastName(customerRequestDto.getLastName());
        customer.setEmail(customerRequestDto.getEmail());
        customer.setPhone(customerRequestDto.getPhone());
        customer.setAddress(customerRequestDto.getAddress());
        Customer savedCustomer = customerRepository.save(customer);
        return new CustomerResponseDto(savedCustomer.getId(),savedCustomer.getFirstName(),savedCustomer.getLastName(),savedCustomer.getEmail(),savedCustomer.getPhone(),savedCustomer.getAddress());

    }

    /**
     * Updates an existing customer with the provided details.
     *
     * @param customerId the ID of the customer to update (must not be null)
     * @param customerRequestDto the customer data used for the update (must not be null)
     * @return a CustomerResponseDto containing the updated customer information
     */
    @Transactional
    public CustomerResponseDto updateCustomer(Long customerId, CustomerRequestDto customerRequestDto) {

        if (customerId == null) {
            throw new NullArgumentException("Customer ID cannot be null.");
        }

        if (customerRequestDto == null) {
            throw new NullArgumentException("Customer request cannot be null.");
        }

        if (!StringUtils.hasText(customerRequestDto.getFirstName())) {
            throw new NullArgumentException("First name cannot be empty.");
        }
        if (!ValidationConstants.NAME_PATTERN.matcher(customerRequestDto.getFirstName()).matches()) {
            throw new InvalidPatternException("First name must contain only alphabets.");
        }
        if (!StringUtils.hasText(customerRequestDto.getLastName())) {
            throw new NullArgumentException("Last name cannot be empty.");
        }
        if (!ValidationConstants.NAME_PATTERN.matcher(customerRequestDto.getLastName()).matches()) {
            throw new InvalidPatternException("Last name must contain only alphabets.");
        }
        if (!StringUtils.hasText(customerRequestDto.getEmail())) {
            throw new NullArgumentException("Email cannot be empty.");
        }
        if (!StringUtils.hasText(customerRequestDto.getPhone())) {
            throw new NullArgumentException("Phone number cannot be empty.");
        }

        if (!customerRequestDto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new InvalidEmailException("Invalid email format: " + customerRequestDto.getEmail());
        }

        if (!customerRequestDto.getPhone().matches("^\\d{10}$")) {
            throw new InvalidPhoneException("Phone number must be exactly 10 digits.");
        }


        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

            existingCustomer.setFirstName(customerRequestDto.getFirstName());
            existingCustomer.setLastName(customerRequestDto.getLastName());
            existingCustomer.setEmail(customerRequestDto.getEmail());
            existingCustomer.setPhone(customerRequestDto.getPhone());
            existingCustomer.setAddress(customerRequestDto.getAddress());

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return new CustomerResponseDto(updatedCustomer.getId(), updatedCustomer.getFirstName(), updatedCustomer.getLastName(), updatedCustomer.getEmail(), updatedCustomer.getPhone(), updatedCustomer.getAddress());
    }

    /**
     * Creates a reward transaction for the specified customer.
     *
     * @param customerId the unique identifier of the customer (must not be null)
     * @param transactionRequestDto the transaction request payload containing purchase amount and date
     * @return a TransactionResponseDto containing the purchase amount, purchase date, rewards points, and customer details
     */
     @Transactional
    public TransactionResponseDto createRewards(Long customerId, TransactionRequestDto transactionRequestDto) {

         if (customerId == null) {
             throw new NullArgumentException("Customer ID cannot be null.");
         }

         if (transactionRequestDto == null) {
             throw new NullArgumentException("Customer Transaction request cannot be null.");
         }

         if(transactionRequestDto.getPurchaseAmount() == null){
             throw new NullArgumentException("Customer Purchase Amount cannot be null.");
         }

         if (transactionRequestDto.getPurchaseAmount() <= 0) {
             throw new InvalidPurchaseAmountException("Purchase amount must be greater than zero. Provided: $" + transactionRequestDto.getPurchaseAmount());
         }

         CustomerResponseDto customerResponseDto = new CustomerResponseDto();
        Double rewardsPoints = 0.0;
        Integer spent = transactionRequestDto.getPurchaseAmount();

         Customer customer = customerRepository.findById(customerId)
                 .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (transactionRequestDto.getPurchaseDate() == null) {
            Calendar calendar = Calendar.getInstance();
            java.sql.Date sqlDate = new Date((calendar.getTime()).getTime());
            transactionRequestDto.setPurchaseDate(sqlDate);
        }

        if (spent != null) {
            rewardsPoints = calculateRewardPoints(spent);
        }
        CustomerRewards customerSpendRewards = new CustomerRewards();
        customerSpendRewards.setCustomer(customer);
        customerSpendRewards.setPurchaseAmount(transactionRequestDto.getPurchaseAmount());
        customerSpendRewards.setRewardsPoints(rewardsPoints);
        customerSpendRewards.setPurchaseDate(transactionRequestDto.getPurchaseDate());

        CustomerRewards customerRewards = customerRewardRepository.save(customerSpendRewards);

        customerResponseDto.setId(customerRewards.getCustomer().getId());
        customerResponseDto.setFirstName(customerRewards.getCustomer().getFirstName());
        customerResponseDto.setLastName(customerRewards.getCustomer().getLastName());
        customerResponseDto.setEmail(customerRewards.getCustomer().getEmail());
        customerResponseDto.setPhone(customerRewards.getCustomer().getPhone());
        customerResponseDto.setAddress(customerRewards.getCustomer().getAddress());

        return  new TransactionResponseDto(customerRewards.getPurchaseAmount(),customerRewards.getPurchaseDate(),customerRewards.getRewardsPoints(),customerResponseDto);

    }

    /**
     * Retrieves the monthly rewards for the specified customer for transactions in the last three months.
     *
     * @param customerId the unique identifier of the customer
     * @return a CustomerTransactionsDto containing the customer's details, a list of monthly reward transactions, and the total reward points
     * @throws NullArgumentException if the customerId is null
     * @throws CustomerNotFoundException if no customer is found with the provided customerId
     */
    public CustomerTransactionsDto getCustomerMonthlyRewards(Long customerId){

        if (customerId == null) {
            throw new NullArgumentException("Customer ID cannot be null.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        LocalDate thresholdDate = LocalDate.now().minusMonths(3);

        // Filtering transactions for the last 3 months
        List<CustomerRewards> recentTransactions = customer.getTransactions().stream()
                .filter(tx -> !tx.getPurchaseDate().toLocalDate().isBefore(thresholdDate))
                .collect(Collectors.toList());

        // Aggregating transaction amounts per month while keeping the purchase date
        Map<Integer, List<MonthlyRewardDto>> monthlyAggregates = recentTransactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getPurchaseDate().toLocalDate().getMonthValue(), // Grouping by month
                        Collectors.mapping(
                                tx -> new MonthlyRewardDto(
                                        tx.getPurchaseDate(),
                                        calculateRewardPoints(tx.getPurchaseAmount())
                                ),
                                Collectors.toList()
                        )
                ));

        double totalRewards = recentTransactions.stream()
                .mapToDouble(tx -> calculateRewardPoints(tx.getPurchaseAmount()))
                .sum();

        List<MonthlyRewardDto> monthlyRecords = monthlyAggregates.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .sorted(Comparator.comparing(MonthlyRewardDto::getPurchaseDate).reversed())
                .collect(Collectors.toList());

        return new CustomerTransactionsDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                monthlyRecords,
                totalRewards
        );

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


}
