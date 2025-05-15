package com.customer_rewards.rewards_calculation.util.constants;

import java.util.regex.Pattern;

public class ValidationConstants {

    public static final String NAME_REGEX = "^[A-Za-z]+$";

    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
}
