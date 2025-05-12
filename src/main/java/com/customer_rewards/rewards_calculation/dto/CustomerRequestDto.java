package com.customer_rewards.rewards_calculation.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequestDto {

    @NotBlank(message = "First Name is mandatory")
    private String firstName;
    @NotBlank(message = "Last Name is mandatory")
    private String lastName;
    @Email(message = "Email should be valid")
    private String email;
    @Pattern(regexp = "^[0-9]+$", message = "Only numbers allowed in Phone number")
    @Size(min = 10, max = 10, message = "Phone number should have 10 digits")
    private String phone;
    private String address;

}
