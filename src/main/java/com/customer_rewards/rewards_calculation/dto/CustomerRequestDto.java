package com.customer_rewards.rewards_calculation.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequestDto {

    @NotBlank(message = "First Name is mandatory")
    @Pattern(regexp = "^[A-Za-z]+$", message = "First name must contain only alphabetic characters")
    @Schema(description = "First name of the Customer", example = "John")
    private String firstName;
    @Pattern(regexp = "^[A-Za-z]+$", message = "Last name must contain only alphabetic characters")
    @Schema(description = "Customer's last name", example = "Wayne")
    @NotBlank(message = "Last Name is mandatory")
    private String lastName;
    @Email(message = "Email should be valid")
    @Schema(description = "Valid email address of the Customer", example = "john.wayne@gmail.com")
    private String email;

    @Pattern(regexp = "^[0-9]+$", message = "Only numbers allowed in Phone number")
    @Size(min = 10, max = 10, message = "Phone number should have 10 digits")
    @Schema(description = "Valid Phone number of the Customer", example = "8754809980")
    private String phone;
    @Schema(description = "Address of the Customer", example = "123, Park Street")
    private String address;

}
