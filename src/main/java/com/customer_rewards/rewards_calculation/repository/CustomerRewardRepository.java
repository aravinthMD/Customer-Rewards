package com.customer_rewards.rewards_calculation.repository;

import com.customer_rewards.rewards_calculation.entity.CustomerRewards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRewardRepository extends JpaRepository<CustomerRewards, Long> {

}
