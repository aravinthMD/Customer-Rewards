package com.customer_rewards.rewards_calculation.service;

import com.customer_rewards.rewards_calculation.dto.*;
import com.customer_rewards.rewards_calculation.entity.Customer;
import com.customer_rewards.rewards_calculation.entity.CustomerRewards;
import com.customer_rewards.rewards_calculation.exception.customException.CustomerNotFoundException;
import com.customer_rewards.rewards_calculation.exception.customException.UserAlreadyExistsException;
import com.customer_rewards.rewards_calculation.repository.CustomerRepository;
import com.customer_rewards.rewards_calculation.repository.CustomerRewardRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto){

        if (customerRepository.existsByEmail(customerRequestDto.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Customer with the email " + customerRequestDto.getEmail() + " already exists."
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

    @Transactional
    public CustomerResponseDto updateCustomer(Long customerId, CustomerRequestDto customerRequestDto) {

        Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        if (customerRequestDto.getFirstName() != null) {
            existingCustomer.setFirstName(customerRequestDto.getFirstName());
        }
        if (customerRequestDto.getLastName() != null) {
            existingCustomer.setLastName(customerRequestDto.getLastName());
        }
        if (customerRequestDto.getEmail() != null) {
            existingCustomer.setEmail(customerRequestDto.getEmail());
        }
        if (customerRequestDto.getPhone() != null) {
            existingCustomer.setPhone(customerRequestDto.getPhone());
        }
        if (customerRequestDto.getAddress() != null) {
            existingCustomer.setAddress(customerRequestDto.getAddress());
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);

        return new CustomerResponseDto(updatedCustomer.getId(), updatedCustomer.getFirstName(), updatedCustomer.getLastName(), updatedCustomer.getEmail(), updatedCustomer.getPhone(), updatedCustomer.getAddress());
    }

     @Transactional
    public TransactionResponseDto createRewards(Long customerId, TransactionRequestDto transactionRequestDto) {

        CustomerResponseDto customerResponseDto = new CustomerResponseDto();
        Double rewardsPoints = 0.0;
        Integer spent = transactionRequestDto.getPurchaseAmount();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

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

    public CustomerTransactionsDto getCustomerMonthlyRewards(Long customerId){

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

        LocalDate thresholdDate = LocalDate.now().minusMonths(3);

        // Filtering transactions for the last 3 months
        List<CustomerRewards> recentTransactions = customer.getTransactions().stream()
                .filter(tx -> !tx.getPurchaseDate().toLocalDate().isBefore(thresholdDate))
                .collect(Collectors.toList());

        recentTransactions.forEach(tx -> System.out.println("Transaction Date: " + tx.getPurchaseDate().toLocalDate()));

        // Aggregating transaction amounts per month
        Map<Integer, Double> monthlyAggregates = recentTransactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getPurchaseDate().toLocalDate().getMonthValue(),
                        Collectors.summingDouble(tx -> calculateRewardPoints(tx.getPurchaseAmount()))
                ));

        Double totalRewards  = monthlyAggregates.entrySet().stream().mapToDouble(e -> e.getValue()).sum();


        // Converting to DTO format
        List<MonthlyRewardDto> monthlyRecords = monthlyAggregates.entrySet().stream()
                .map(e -> new MonthlyRewardDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MonthlyRewardDto::getMonth).reversed())
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
